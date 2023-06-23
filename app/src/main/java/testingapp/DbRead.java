package testingapp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVWriter;


public class DbRead {

    private static Logger logger = LoggerFactory.getLogger(DbLoad.class);
    
    public static int run_queries(File file, String outputfolder, String dbpath) {

        //æ
        try{
            Class.forName("org.sqlite.JDBC");
            var conn = DriverManager.getConnection("jdbc:sqlite:"+dbpath);
            var stmt = conn.createStatement();

            Scanner sqlReader = new Scanner(file);
            while (sqlReader.hasNextLine()) {
                String[] data = sqlReader.nextLine().split("æ");
                String rulename = data[0];
                File outputFile = new File(outputfolder, rulename+".csv");
                FileWriter filewriter = new FileWriter(outputFile);
                CSVWriter writer = new CSVWriter(filewriter, 'æ',
                                                    CSVWriter.NO_QUOTE_CHARACTER,
                                                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                                                    CSVWriter.DEFAULT_LINE_END);
                logger.info(String.format("Executing %s", rulename));
                ResultSet rs = stmt.executeQuery(data[1].trim());
                writer.writeAll(rs, true);
                writer.close();
                filewriter.close();
            }
            sqlReader.close();
        }
        catch (IOException | SQLException | ClassNotFoundException err) {
            logger.error(err.getMessage());
            return 1;
        }
        return 0;
    }
}
