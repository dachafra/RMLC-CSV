package es.upm.fi.dia.oeg.engine;



import es.upm.fi.dia.oeg.rmlc.api.model.*;
import es.upm.fi.dia.oeg.rmlc.api.binding.jena.JenaRMLCMappingManager;
import es.upm.fi.dia.oeg.rmlc.api.model.impl.InvalidRMLCMappingException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class RMLCProcess{

    private HashMap<String,Collection<TriplesMap>> mapping;
    private RDBConexion rdbConexion;
    private JSONArray datasets;
    private String rdb="";
    private static final Logger _log = LoggerFactory.getLogger(RMLCProcess.class);


    public RMLCProcess(JSONObject config){
        mapping = new HashMap<>();
        this.datasets = config.getJSONArray("datasets");
        rdbConexion = new RDBConexion(config);
    }

    public void run(){
        readMapping();
        checkDatatypes();
        createRDB();
    }

    public void readMapping(){
        for(Object object: datasets) {
            JSONObject dataset = (JSONObject) object;
            JenaRMLCMappingManager mm = JenaRMLCMappingManager.getInstance();
            Model m = ModelFactory.createDefaultModel();
            m = m.read(getMapping(dataset.getString("mapping"),dataset.getString("databaseName")));
            try {
                Collection<TriplesMap> triplesMaps = mm.importMappings(m);
                mapping.put(dataset.getString("databaseName"),triplesMaps);
            } catch (InvalidRMLCMappingException e) {
                _log.error("Exception in read mapping: " + e.getMessage());
            }
        }
    }

    public String getMapping(String url,String name){
        String content=url;
        try {
            if (url.matches("http.*")) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
                content = reader.lines().collect(Collectors.joining());
                File f = new File("datasets/mappings/"+name+".rmlc.ttl");
                BufferedWriter writer = new BufferedWriter(new FileWriter(f, false));
                writer.write(content);
                writer.close();
            }
        }catch (Exception e){
            _log.error("Error getting the mapping content: "+e.getMessage());
        }
        return content;
    }

    public void checkDatatypes(){
        for(Map.Entry<String, Collection<TriplesMap>> entry : mapping.entrySet()) {
            this.checkTypes(entry.getValue(),entry.getKey());
        }
    }


    public void createRDB(){
        for(Map.Entry<String, Collection<TriplesMap>> entry : mapping.entrySet()) {
            String name = entry.getKey();
            CSVTransformation csvTransformation = new CSVTransformation(null, null, name,false);
            HashMap<String,String[]> firstRow = csvTransformation.getFirstRow();
            entry.getValue().forEach(triplesMap -> {
                for(Map.Entry<String,String[]> firstRowEntry : firstRow.entrySet()){
                    String tableName =((SQLBaseTableOrView) triplesMap.getLogicalTable()).getTableName().toLowerCase();
                    if(firstRowEntry.getKey().equals(tableName)){
                        rdb+=this.createTable(triplesMap, firstRowEntry.getValue(), tableName.toUpperCase());
                        break;
                    }
                }

            });

            try{
                BufferedWriter writer = new BufferedWriter
                        (new OutputStreamWriter(new FileOutputStream("sql/"+name+".sql"), StandardCharsets.UTF_8));
                writer.write(rdb);
                writer.close();
                rdbConexion.createDatabase(name,rdb);
                rdb="";
            }catch (IOException e){
                _log.error("Error writing the SQL file: "+e.getMessage());
            }
        }

    }


    private void checkTypes(Collection<TriplesMap> triplesMaps, String rdb){
        for (TriplesMap triplesMap : triplesMaps) {
            for(PredicateObjectMap predicateObjectMap: triplesMap.getPredicateObjectMaps()){
                for(ObjectMap objectMap: predicateObjectMap.getObjectMaps()) {
                    if (objectMap.getDatatype() != null) {
                        StringTokenizer st = new StringTokenizer(objectMap.getDatatype().getIRIString(),"#");
                        if(st.nextToken().equals("http://www.w3.org/2001/XMLSchema")) {
                            //checking numbers
                            CSVTransformation csvTransformation = new CSVTransformation(objectMap.getColumn(),((SQLBaseTableOrView) triplesMap.getLogicalTable()).getTableName(), rdb, true);
                            String type = st.nextToken();
                            if (type.matches("decimal|integer|double")) {
                                //ToDo accept SQL views.
                                csvTransformation.transformNumbers();
                            }
                            //checking dates
                            else if (type.equals("date")) {
                                csvTransformation.transformDates();
                            }
                            //ToDo checking dateTimes
                            else if (type.equals("dateTime")) {
                                csvTransformation.transformDateTimes();
                            } else if (type.equals("time")) { //does it make sense?
                                csvTransformation.transformTime();
                            } else if (type.equals("boolean")) {
                                csvTransformation.transformBoolean();
                            }
                        }
                    }
                }
            }
        }

    }


    private String createTable(TriplesMap tripleMap, String[] firstCSVRow, String tableName){
        ArrayList<String> primaryKeys = getPrimaryKeys(tripleMap.getSubjectMap());

        String table="DROP TABLE IF EXISTS "+tableName+";\nCREATE TABLE "+tableName+" ";
        table+="(";

        for(String field : firstCSVRow){
            table += "`"+ field.toUpperCase().trim()+"` "+getTypeFromColumn(tripleMap.getPredicateObjectMaps(),field)+",";
        }

        table=table.substring(0,table.length()-1);
        if(!primaryKeys.isEmpty()) {
            table += ",PRIMARY KEY (";
            for (String p : primaryKeys) {
                table += p + ",";
            }
            table=table.substring(0,table.length()-1);
            table += ")";
        }

        table+=");\n";
        return table;
    }

    private ArrayList<String> getPrimaryKeys(SubjectMap s){
        ArrayList<String> primaryKeys = new ArrayList<>();

        if(s.getColumn()!=null){
            primaryKeys.add(s.getColumn());
        }
        else if(s.getTemplateString()!=null){
            for(String t : s.getTemplate().getColumnNames()){
                primaryKeys.add(t);
            }
        }
        return primaryKeys;

    }


    private String getTypeFromColumn(List<PredicateObjectMap> predicates, String columnName){
        String type=null;
        for(PredicateObjectMap p: predicates){
            for(ObjectMap o: p.getObjectMaps()){
                if(this.checkObject(o,columnName)) {
                    type = getTypeFromObject(o);
                    break;
                }
            }
        }
        if(type==null){
            type="VARCHAR(200)";
        }
        return type;
    }

    private boolean checkObject(ObjectMap o, String columnName){
        List<String> columns = this.getColumnNames(o);
        if(columns.contains(columnName.toUpperCase())){
            return true;
        }
        else
            return false;
    }

    private String getTypeFromObject(ObjectMap object){
        String type;

        if(object.getDatatype()!=null){
            String[] st = object.getDatatype().getIRIString().split("#");
            if(st[0].equals("http://www.w3.org/2001/XMLSchema")) {
                String semanticType = st[1];
                if (semanticType.matches("integer")) {
                    type = "INT";
                } else if (semanticType.matches("double|decimal")) {
                    type = "DOUBLE";
                }
                //checking dates
                else if (semanticType.matches("date")) {
                    type = "DATE";
                } else if (semanticType.matches("boolean")) {
                    type = "BOOL";
                } else {
                    type = "VARCHAR(200)";
                }
            }
            else {
                type = "VARCHAR(200)";
            }

        }
        else {
            type="VARCHAR(200)";
        }

        return type;
    }


    private List<String> getColumnNames(ObjectMap o){
        List<String> columNames = new ArrayList<>();
        if(o.getColumn()!=null){
            columNames.add(o.getColumn());
        }
        else if (o.getTemplate()!=null){
            columNames.addAll(o.getTemplate().getColumnNames());
        }
        return  columNames;
    }


}
