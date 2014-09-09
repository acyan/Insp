/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package insp;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

/**
 *
 * @author dasha
 */
public class FXMLDocumentController implements Initializable {
    DBAdapter database;
    
    @FXML
 //   private Label label;
    private TableView<Site> tableView;
    

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ObservableList<Site> sites = FXCollections.<Site>observableArrayList();
        sites.add(new Site("http://google.com", true));
        database=new DBAdapter();
        try {
            database.fetchSites(tableView);
        } catch (InterruptedException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
