/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package testingapp.Services;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dhatim.fastexcel.reader.Cell;

/**
 *
 * @author shyjuk
 */
public final class DbLoader {

    private final static Logger logger = LoggerFactory.getLogger(DbLoader.class);
    private final static int DEFAULT_VARCHAR_SIZE = 30000;
    private final static String CLASSNAME = "org.h2.Driver";
    
    
    private static List<String> fix_header(List<String> header){
        List<String> final_list = new ArrayList<>();
        Set<String> checked_list = new HashSet<>();
        header.forEach((String item) -> {
            if (!checked_list.contains(item)){
                checked_list.add(item);
                final_list.add(item);
            }
            else{
                String new_item = item + "_1";
                int suffix = 1;
                while(checked_list.contains(new_item)){
                    suffix = Integer.parseInt(new_item.split("_")[-1])+1;
                    new_item = String.format("%s_%s", new_item.split("_")[0],suffix);
                }
                checked_list.add(new_item);
                final_list.add(new_item);
            }
        });
        return final_list;
    }
    
    
    private static List<List<String>> read_excel(File inputfile) throws Exception{
        List<List<String>> data = new ArrayList<>();
        FileInputStream file = new FileInputStream(inputfile);
        try (ReadableWorkbook wb = new ReadableWorkbook(file)) {
            Sheet sheet = wb.getFirstSheet();
            Stream<Row> rows = sheet.openStream();
            rows.forEach((Row r) -> {
                List<String> rowval = new ArrayList<>();
                for (Cell cell: r){
                    if (cell != null){
                        rowval.add(cell.getRawValue());
                    }
                }
                data.add(rowval);
            });
        }        
        return data;
    }
    
    
    private static List<List<String>> read_csv(File inputfile, char delimiter) throws Exception{
        List<List<String>> data = new ArrayList<>();
        FileReader fileReader = new FileReader(inputfile);
        try (CSVReader csvReader = new CSVReaderBuilder(fileReader)
                .withCSVParser(
                        new CSVParserBuilder()
                                .withSeparator(delimiter)
                                .build())
                .build()) {
            String[] nextRecord;
            while((nextRecord = csvReader.readNext())!= null){
                List<String> row = new ArrayList<>();
                row.addAll(Arrays.asList(nextRecord));
                data.add(row);
            }
        }
        return data;
    }
    
    
    private static void write_db(File file, String dbpath, List<List<String>> data) {
        
        Connection conn = null;
        Statement stmt = null;
        
        String filename = file.getName();
        String tablename = filename.substring(0, filename.lastIndexOf(".")).replaceAll("[-. ]", "_");
        List<String> header = data.get(0);
        List<List<String>> rowvals = data.subList(1, data.size());
        header = fix_header(
                header.stream().map(e -> {
                        return e.trim().replaceAll("[-. ]", "_").toUpperCase();
                    }).collect(Collectors.toList())
                );
        
        try {
            Class.forName (CLASSNAME); 
            conn = DriverManager.getConnection("jdbc:h2:"+dbpath);
            String create_stmt = "CREATE TABLE IF NOT EXISTS "+tablename+"("
                                + String.join(
                                        String.format(" VARCHAR(%d),",DEFAULT_VARCHAR_SIZE)
                                        , header) 
                                        + String.format(" VARCHAR(%d))",DEFAULT_VARCHAR_SIZE);
            
            stmt = conn.createStatement();
            try{
                stmt.executeUpdate(create_stmt);
                logger.info(String.format("Table %s created.", tablename));
            }
            catch(SQLException e){
                logger.warn(e.getMessage());
            }
            int header_size = header.size();
            int count = 0;
            final int total_count = rowvals.size();
            for (List<String> row: rowvals){
                if (row.size() > header_size){
                    row = row.subList(0, header_size-1);
                }
                String rowvalStr = row.stream()
                                        .map(s -> String.format("\'%s\'", s.replace("'","\"")))
                                        .collect(Collectors.joining(","));
                String insert_stmt = "INSERT INTO "+tablename+" ("+
                                            String.join(",", header)+")"+    
                                            " VALUES (" + rowvalStr + " )";
                try{
                    stmt.execute(insert_stmt);
                    count++;
                } catch (SQLException e){
                    
                }
            }
            logger.info(String.format("Inserted %d rows out of %d rows", count, total_count));
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        finally{
            if (stmt != null){
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    logger.error(ex.getMessage());
                }
            }
            if (conn != null){
                try {
                    conn.close();
                } catch (SQLException ex) {
                    logger.error(ex.getMessage());
                }
            }
        }
        
    }
    
    
    public static void load_excel(File[] files, String dbpath){
        List<Thread> threads = new ArrayList<>();
        for (File file: files){
            Runnable runnable = () -> {
                try{
                    logger.info(String.format("Reading File: %s", file.getName()));
                    List<List<String>> data = read_excel(file);
                    logger.info(String.format("Completed Reading File: %s", file.getName()));
                    if (!data.isEmpty()){
                        logger.info(String.format("Number of rows read: %d",data.size()));
                        logger.info(String.format("Number of columns: %d", data.get(0).size()));
                        write_db(file, dbpath, data);
                    }
                    else{
                        logger.info("No records found.");
                    }
                }
                catch(Exception e){
                    logger.error(String.format("%s cannot be processed.",file.getName()), e);
                    logger.error(e.getMessage());
                }
            };
            Thread thread = new Thread(runnable);
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
        }
    }
    
    
    public static void load_csv(File[] files, String dbpath, char delimiter){
        List<Thread> threads = new ArrayList<>();
        for (File file: files){
            Runnable runnable = () -> {
                try{
                    logger.info(String.format("Reading File: %s", file.getName()));
                    List<List<String>> data = read_csv(file, delimiter);
                    logger.info(String.format("Completed Reading File: %s", file.getName()));
                    if (!data.isEmpty()){
                        logger.info(String.format("Number of rows read: %d",data.size()));
                        logger.info(String.format("Number of columns: %d", data.get(0).size()));
                        write_db(file, dbpath, data);
                    }
                    else{
                        logger.info("No records found.");
                    }
                }
                catch(Exception e){
                    logger.error(String.format("%s cannot be processed.",file.getName()), e);
                    logger.error(e.getMessage());
                }
            };
            Thread thread = new Thread(runnable);
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
        }
    }
}
