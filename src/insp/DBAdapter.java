/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package insp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableView;

/**
 *
 * @author dasha
 */
public class DBAdapter {
    private ExecutorService databaseService;
    private Future databaseSetup;
    private static final Logger logger = Logger.getLogger(Insp.class.getName());

    public DBAdapter() {
        this.databaseService = Executors.newFixedThreadPool(1, new DatabaseThreadFactory());
        DBSetupTask setup = new DBSetupTask();
        this.databaseSetup = databaseService.submit(setup);
        try {
            this.databaseSetup.get();
        } catch (InterruptedException ex) {
            Logger.getLogger(DBAdapter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(DBAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    

  

    private Connection getConnection() throws ClassNotFoundException, SQLException {
      logger.info("Getting a database connection");
      Class.forName("org.h2.Driver");
      return DriverManager.getConnection("jdbc:h2:~/test", "sa", "");
    }
    
    public void fetchSites(TableView<Site> tableView) throws InterruptedException, ExecutionException{
      final FetchNamesTask fetchNamesTask = new FetchNamesTask();
      databaseService.submit(fetchNamesTask).get();
      fetchNamesTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

          @Override
          public void handle(WorkerStateEvent event) {
            ObservableList<Site> s= fetchNamesTask.getValue();
            tableView.setItems(s);
          }
      });
    }
  
    static class DatabaseThreadFactory implements ThreadFactory {
      static final AtomicInteger poolNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable runnable) {
          Thread thread = new Thread(runnable, "Database-Connection-" + poolNumber.getAndIncrement() + "-thread");
          thread.setDaemon(true);

          return thread;
        }
    }
    abstract class DBTask<T> extends Task<T> {
      DBTask() {
        setOnFailed(new EventHandler<WorkerStateEvent>() {
          @Override public void handle(WorkerStateEvent t) {
            logger.log(Level.SEVERE, null, getException());
          }
        });
      }
    }
    
    class DBSetupTask<Void> extends DBTask<Void>{

        @Override
        protected Void call() throws Exception {
            try(Connection con = getConnection()){
                if(!schemaExists(con)){
                    createSchema(con);
                    populateDatabase(con);
                }
            }
            return null;
            
        }
        
        private boolean schemaExists(Connection con) {
          logger.info("Checking for Schema existence");      
          try {
            Statement st = con.createStatement();      
            st.executeQuery("select count(*) from sites");
            logger.info("Schema exists");      
          } catch (SQLException ex) {
            logger.info("Existing DB not found will create a new one");
            return false;
          }

          return true;
        }

        private void createSchema(Connection con) throws SQLException {
          logger.info("Creating schema");
          Statement st = con.createStatement();
          String table = "create table IF NOT EXISTS sites(id int NOT NULL auto_increment, name varchar(64), change_freq boolean, change_number int, unique(name), primary key(id))";
          st.executeUpdate(table);
          logger.info("Created schema");
        } 
        private void populateDatabase(Connection con) throws SQLException {
          logger.info("Populating database");      
          Statement st = con.createStatement();      
            st.execute("INSERT INTO sites(name,change_freq) SELECT * FROM(SELECT 'http://google.com','true') AS tmp WHERE NOT EXISTS(SELECT name FROM sites WHERE name = 'http://google.com') LIMIT 1");
            st.execute("INSERT INTO sites(name,change_freq) SELECT * FROM(SELECT 'http://yandex.ru','true') AS tmp WHERE NOT EXISTS(SELECT name FROM sites WHERE name = 'http://yandex.ru') LIMIT 1");
          logger.info("Populated database");
        } 
        

    }

  class FetchNamesTask extends DBTask<ObservableList<Site>> {
    @Override protected ObservableList<Site> call() throws Exception {
      try (Connection con = getConnection()) {
          ObservableList<Site> result = fetchNames(con);
        return result;
      }
    }
    
    private ObservableList<Site> fetchNames(Connection con) throws SQLException {
      logger.info("Fetching names from database");
      ObservableList<Site> sites = FXCollections.observableArrayList();
 
      Statement st = con.createStatement();      
      ResultSet result = st.executeQuery("select * from sites");
      
        while (result.next()) {
            String name = result.getString("NAME");
            Boolean change = result.getBoolean("change_freq");
            sites.add(new Site(name, change));
            System.out.println(result.getString("NAME")+" "+ result.getBoolean("change_freq"));
        }
 
      logger.info("Found " + sites.size() + " names");
 
      return sites;
    }
  }
}
