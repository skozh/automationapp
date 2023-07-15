/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package testingapp.Services;

import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author shyjuk
 */
public class RulesRunner {
    
    private final static Logger logger = LoggerFactory.getLogger(RulesRunner.class);
    private final static String CLASSNAME = "org.h2.Driver";

    public static void parallel_rules_runner(String dbfilepath, String rulefilepath, char delimiter, String outputdir){
        
        try{
            Class.forName(CLASSNAME);
            String dbfilename = dbfilepath.replace(".mv.db","");
            List<Thread> threads = new ArrayList<>();
            
            Scanner sqlReader = new Scanner(new File(rulefilepath));
            while (sqlReader.hasNextLine()){
                String[] data = sqlReader.nextLine().split("\\"+delimiter);
                String rulename = data[0];
                Runnable runnable = () -> {
                    FileWriter filewriter = null;
                    try{
                        Connection conn = DriverManager.getConnection("jdbc:h2:"+dbfilename+";AUTO_SERVER=TRUE");
                        Statement stmt = conn.createStatement();
                        logger.info(String.format("Executing %s", rulename));
                        ResultSet rs = stmt.executeQuery(data[1].trim());
                        File outputFile = new File(outputdir, rulename+".csv");
                        filewriter = new FileWriter(outputFile);
                        CSVWriter writer = new CSVWriter(filewriter, delimiter,
                                CSVWriter.NO_QUOTE_CHARACTER,
                                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                                CSVWriter.DEFAULT_LINE_END);
                        writer.writeAll(rs, true);
                        writer.close();
                        conn.close();
                    }
                    catch (IndexOutOfBoundsException err){
                        logger.error("Check the query file and make sure delimiter is correct!", err);
                    }
                    catch (IOException ex){
                        java.util.logging.Logger.getLogger(RulesRunner.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SQLException ex) {
                        logger.error(ex.getMessage());
                    } finally {
                        try {
                            filewriter.close();
                        } catch (IOException ex) {
                            logger.error(ex.getMessage());
                        }
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
        catch (Exception err){
            logger.error(err.getMessage());
        }
    }
}
