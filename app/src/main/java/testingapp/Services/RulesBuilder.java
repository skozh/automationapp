/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package testingapp.Services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author shyjuk
 */
public class RulesBuilder {

    public Map<String, List<String>> getTableMap() {
        return tableMap;
    }
    
    private final static Logger logger = LoggerFactory.getLogger(RulesBuilder.class);
    private final static String CLASSNAME = "org.h2.Driver";
    private final static String READ_SQL = "SELECT TABLE_NAME, COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME IN (SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE ='TABLE') ORDER BY TABLE_NAME, COLUMN_NAME"; 
    Map<String, List<String>> tableMap;
    
    public RulesBuilder(String dbfilepath) {
        
        Connection conn = null;
        Statement stmt = null;
        try {
            this.tableMap = new HashMap<>();
            Class.forName(CLASSNAME);
            String dbfilename = dbfilepath.replace(".mv.db","");
            conn = DriverManager.getConnection("jdbc:h2:"+dbfilename+";AUTO_SERVER=TRUE");
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(READ_SQL);
            while(rs.next()){
                String tablename = rs.getString("TABLE_NAME");
                String columnname = rs.getString("COLUMN_NAME");
                if (this.tableMap != null){
                    if (this.tableMap.containsKey(tablename)){
                        this.tableMap.get(tablename).add(columnname);
                    }
                    else{
                        List<String> columnList = new ArrayList<>();
                        columnList.add(columnname);
                        this.tableMap.put(tablename, columnList);
                    }
                }
                else{
                    List<String> columnList = new ArrayList<>();
                    columnList.add(columnname);
                    this.tableMap.put(tablename, columnList);
                }
            }
        } catch (ClassNotFoundException | SQLException ex) {
            logger.error(ex.getMessage());
        }
        finally{
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                logger.error(ex.getMessage());
            }
        }   
    }
    
    
    public static void writeToFile(List<String> data, String filepath){
        
        FileWriter filewriter = null;
        try {
            File outputFile = new File(filepath);
            logger.info(String.format("Writing to file %s", outputFile.getAbsolutePath()));
            filewriter = new FileWriter(outputFile);
            for (String row: data){
                filewriter.write(row+"\n");
            }
            logger.info(String.format("%d rules saved.", data.size()));
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
        finally{
            if (filewriter != null){
                try {
                    filewriter.close();
                } catch (IOException ex) {
                    logger.error(ex.getMessage());
                }
            }
        }
    }
}
