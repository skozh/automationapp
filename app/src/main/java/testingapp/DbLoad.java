package testingapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.dhatim.fastexcel.reader.Cell;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;


public class DbLoad {

    private static Logger logger = LoggerFactory.getLogger(DbLoad.class);
    private static int DEFAULT_VARCHAR_SIZE=10000;

    public static List<List<String>> readExcel(String filepath) throws IOException{

        List<List<String>> data = new ArrayList<>();
        FileInputStream file = new FileInputStream(filepath);
        ReadableWorkbook wb = new ReadableWorkbook(file);
        Sheet sheet = wb.getFirstSheet();
        Stream<Row> rows = sheet.openStream();
        rows.forEach(r -> {
            ArrayList<String> rowval = new ArrayList<>();
            for (Cell cell: r){
                rowval.add(cell.getRawValue());
            }
            data.add(rowval);
        });
        wb.close();
        return data;
    }


    public static List<List<String>> readCsv(String filepath, char delimiter) 
                    throws IOException, CsvValidationException{

        List<List<String>> data = new ArrayList<>();
        FileReader fileReader = new FileReader(filepath);
        CSVReader csvReader = new CSVReaderBuilder(fileReader)
                .withCSVParser(new CSVParserBuilder().withSeparator(delimiter).build())
                .build();
        String[] nextRecord;
        while ((nextRecord = csvReader.readNext()) != null) {
            List<String> row = new ArrayList<String>();
            for (String cell : nextRecord) {
                row.add(cell);
            }
            data.add(row);
        }
        csvReader.close();
        return data;
    }


    public static List<String> fix_header(List<String> header){
        List<String> final_list = new ArrayList<>();
        Set<String> seen_list = new HashSet<>();

        for (String item: header){
            if (!seen_list.contains(item)){
                seen_list.add(item);
                final_list.add(item);
            }
            else{
                String new_item = item + "_1";
                int suffix = 1;
                while (seen_list.contains(new_item)){
                    suffix = Integer.parseInt(new_item.split("_")[-1])+1;
                    new_item = String.format("%s_%s",new_item.split("_")[0],suffix);
                }
                seen_list.add(new_item);
                final_list.add(new_item);
            }
        }
        return final_list;
    }


    public static void writeToDB(File fpath, String dbpath, 
                                List<List<String>> data ) throws SQLException, ClassNotFoundException{

        String filename = fpath.getName().split("\\.", 2)[0].replace("-","_");

        List<String> header = data.get(0);
		List<List<String>> colvals = data.subList(1, data.size());
        header = fix_header(header.stream().map(e -> e.trim().replace(" ","_").toUpperCase()).collect(Collectors.toList())); 
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        Connection conn = DriverManager.getConnection("jdbc:derby:"+dbpath+";create=true");
        String create_stmt = "CREATE TABLE "+filename+"("
                                + String.join(String.format(" VARCHAR(%d),",DEFAULT_VARCHAR_SIZE), header) + String.format(" VARCHAR(%d))",DEFAULT_VARCHAR_SIZE);
        Statement stmt = conn.createStatement();
        try{
            stmt.executeUpdate(create_stmt);
            logger.info(String.format("Table %s Created", filename));
        }
        catch(SQLException e){
            logger.warn(String.format("Table %s might be already present. Please check.", filename));
        }
        int count = 0;
        for (List<String> row: colvals){
            if (row.size()!=0){
                String rowvalStr = row.stream()
                                        .map(s -> String.format("\'%s\'", s))
                                        .collect(Collectors.joining(","));
                String insert_stmt = "INSERT INTO "+filename+" ("+
                                            String.join(",", header)+")"+    
                                            " VALUES (" + rowvalStr + " )";
                try{
                    stmt.execute(insert_stmt);
                    count++;
                } catch (SQLException e){
                    logger.error("Data Insertion failed!", e);
                }
            }
        }
        logger.info(String.format("Inserted %d rows", count));
        stmt.close();
        conn.close();
    }


    public static int run_excel(File[] files, String dbpath){

        int result = 1;
        for (File file: files){
            try{
                List<List<String>> data = readExcel(file.getAbsolutePath());
                writeToDB(file, dbpath, data);
            }
            catch(IOException | ClassNotFoundException | SQLException err){
                logger.error(String.format("%s cannot be processed.", file.getName()), err);
                logger.error(err.getMessage());
                result = 0;
            } 
        }
        return result;
    }


    public static int run_csv(File[] files, String dbpath, char delimiter){

        int result=1;
        for (File file: files){
            try{
                List<List<String>> data = readCsv(file.getAbsolutePath(), delimiter);
                writeToDB(file, dbpath, data);
            }
            catch( IOException | CsvValidationException | SQLException | ClassNotFoundException err){
                logger.error(String.format("%s cannot be processed.", file.getName()), err);
                result = 0;
            }
        }
        return result;
    }
}
