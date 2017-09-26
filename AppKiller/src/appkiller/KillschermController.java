/*
 * SEFLAB SEF01
 * AppKiller
 * (c) 2016 SEF01
 */
package appkiller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import javax.swing.JOptionPane;

/**
 * FXML Controller class
 *
 * @author Alex, Thijs, Joyce
 */
public class KillschermController implements Initializable {

    @FXML
    TextField naam;

    @FXML
    ListView unkillProcess;

    @FXML
    ComboBox<String> eigenLijst;

    @FXML
    TableView<ObservableList> table;

    String waarde;
    ObservableList<String> data;
    String[] strings;
    String line;
    List<String> array = new ArrayList<>();

    //SynTPEnh mag niet gekilld worden, is voor rechtsklik op touchpad enzo
    //HKCMD is controller voor hotkeys, ook niet killen
    String[] lijstProcessen = {"iexplore.exe", "slack.exe", "AGSService.exe", "Atiesrxx.exe", "GoogleUpdate.exe", "SearchIndexer.exe", "Spoolsv.exe", "BackupManagerTray.exe", "Jusched.exe", "reader_sl.exe", "jqs.exe", "Osa.exe", "soffice.exe", "AdobieARM.exe", "AAM Update Notifier.exe", "DivXUpdate.exe", "NeroCheck.exe", "atiptaxx.exe", "RAVCpl64.exe", "Nwiz.exe", "ati2evxx.exe", "CCC.exe"};

    @FXML
    public void killButton(ActionEvent event) throws Exception {
        // als je op kill drukt dan laat ie het lijstje niet vaker zien in de unkillListview
        unkillProcess.getItems().clear();

        try {

            // killen van processen die in de standaardlijst staan
            for (String s : lijstProcessen) {
                if (s != null) {
                    Runtime.getRuntime().exec("taskkill /f /im " + s);
                    unkillProcess.getItems().add(s);
                }
            }

        } catch (Exception e) {
            System.err.println("Exception in killButton(): " + e.getMessage());
        }

        System.out.println("\n");
        System.out.println(Arrays.toString(lijstProcessen));

        JOptionPane.showMessageDialog(null, "All instances of:\n" + Arrays.toString(lijstProcessen) + "\nare killed");
        System.out.println("\nStandaard processen zijn gekilld\n");
    }

    //deleteUnkill verwijdert proces van lijst (zonder te unkillen)
    @FXML
    public void deleteUnkill(ActionEvent event) throws IOException {
        String deleteprocess = (String) unkillProcess.getSelectionModel().getSelectedItem();

        unkillProcess.getItems().remove(deleteprocess);
    }

    //undoButton unkillt geselecteerde process
    @FXML
    public void undoButton(ActionEvent event) throws IOException {
        // naam onthouden van geselecteerde process
        String killedProcess = (String) unkillProcess.getSelectionModel().getSelectedItem();

        // het opstarten van gekillde processen werkt nog niet helemaal foutloos
        Runtime.getRuntime().exec("cmd /c start " + killedProcess);

        JOptionPane.showMessageDialog(null, "" + killedProcess + " has been restarted");
    }

    //inDatabase zet een nieuwe lijst in de database
    @FXML
    public void inDatabase(ActionEvent event) throws Exception {
        //Usage: x.insert([table voor insert], [naam lijst], [observablelist met waardes]);
        SQLInsert insert = new SQLInsert();

        insert.insert(naam.getText(), naam.getText(), unkillProcess.getItems());

        fillCombobox();
    }

    //showProcess populate de lijst met processen vanuit tasklist
    public void showProcess() throws Exception {
        table.getColumns().clear();
        table.getItems().clear();

        /* 
         * code voor het oproepen van de task list met alle processen
         * line is de temp string voor de output van tasklist
         * p roept de tasklist aan
         * sorteren op memorywaarde is onmogelijk door gebruik strings
         * dit is niet op te lossen zonder heel veel tijd en koffie
         * -T
         */
        
        Process p = Runtime.getRuntime().exec(System.getenv("windir") + "\\system32\\" + "tasklist.exe");

        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

        try {

            // zolang tasklist nog data blijft spugen draait deze loop
            while ((line = input.readLine()) != null) {

                //onnodige lines worden niet meegenomen
                if (!(line.equals("") || line.startsWith("Image Name") || line.startsWith("===") || line.startsWith("System Idle") || line.startsWith("Memory"))) {

                    //split de output line op elke spatie
                    strings = line.split("\\s+");

                    //memory footprint converteren naar integer
                    String stringMem = strings[4].replaceAll("\\.", "");
                    int mem = Integer.parseInt(stringMem);

                    //van kb's naar mb's, staat netter
                    mem = mem / 1000;
                    strings[5] = "M";

                    //samenvoegen van memory footprint en SI unit
                    strings[3] = Integer.toString(mem) + " " + strings[5];

                    //laatste entry uit array halen, hebben we niet nodig, is net samengevoegd met 4
                    String[] stringsNew = Arrays.copyOf(strings, (strings.length - 2));

                    //waardes die niet observablelist zijn casten naar observablelist
                    data = FXCollections.observableArrayList();

                    //voeg de huidige bewerkte line toe aan de list data
                    //daarna de inhoud van data toevoegen aan de observablelist table
                    data.addAll(stringsNew);
                    table.getItems().add(data);
                }
            }

            input.close();

        } catch (IOException e) {
            System.err.println("Exception in showProcess(): " + e.getMessage());
        }

        // array voor de namen van de kolommen
        String[] kolomNaam = {"Name", "PID", "Runtime", "Memory"};

        // for loopje voor het aanmaken van kolommen maar ook hier gebruik ik weer array.size() om de lengte van iets te weten maar dit 
        // hoor volgens mij niet en moet anders denk
        for (int i = 0; i < data.size(); i++) {

            // kolom aanmaken
            TableColumn name = new TableColumn(kolomNaam[i]);
            final int j = i;

            // CellValueFactory bouwt de cellen in de tabel op en zorgt ervoor dat je ze per cel kan aanspreken
            // negeer het lambdaexpression verhaal, ik heb niet genoeg verstand van java om dit te fixen lol
            name.setCellValueFactory(new Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {

                @Override
                public ObservableValue<String> call(CellDataFeatures<ObservableList, String> p) {
                    return new SimpleStringProperty(p.getValue().get(j).toString());
                }
            });
            // table vullen met de aangemaakte kolommen
            table.getColumns().addAll(name);
        }
    }

    //code voor de killbutton
    @FXML
    public void kill(ActionEvent event) throws Exception {
        // de rij die je selecteert in de tableview pakt de positie van de eerste(0) kolom
        TablePosition pos = table.getSelectionModel().getSelectedCells().get(0);

        // de positie stop je in een int
        int row = pos.getRow();

        // omdat onze table alleen maar observablelist waardes neemt moet je de item die je selecteert naar observablelist casten.
        // in de volgende code wordt de waarde van de kollom in item gestopt
        ObservableList item = table.getItems().get(row);

        // deze code is niet nodig maar voor de leuk houden we hem erin want ik ben te lui om te testen zonder die code.
        TableColumn name = pos.getTableColumn();

        // en als laatste string data krijgt de waarde van de geselecteerde waarde
        String geselecteerdeProcess = (String) name.getCellObservableValue(item).getValue();

        // hier maken we een proces aan, om windows commands te kunnen gebruiken.
        Process pr;

        // uitvoeren van de processkill code
        try {
            // geforceerd killen van het geselecteerde process
            pr = Runtime.getRuntime().exec("taskkill /f /im " + geselecteerdeProcess);
        } catch (Exception e) {
            System.err.println("Exception in kill(): " + e.getMessage());
        }

        // gekillde process wordt weergegeven in tab unkillProcess
        unkillProcess.getItems().add(geselecteerdeProcess);
        JOptionPane.showMessageDialog(null, "Successfully killed.");

        showProcess();
    }

    //fillCombobox vult de dropdown met de lijsten uit de database
    public void fillCombobox() throws SQLException {

        eigenLijst.getItems().clear();

        //Usage: x.getLijst(), returnwaarde is een stringarray met alle lijsten
        SQLInsert leesLijsten = new SQLInsert();

        eigenLijst.getItems().addAll(leesLijsten.getLijst());

        System.out.println("\nCombobox wordt gevuld");
    }

    //killList regelt het killen van user-defined lijsten
    @FXML
    public void killList(ActionEvent event) throws Exception {

        System.out.println(eigenLijst.getValue());

        String query = "SELECT * FROM " + eigenLijst.getValue();

        SQLInsert lijstContent = new SQLInsert();

        lijstContent.getContent(query);

        for (String s : lijstContent.getContent(query)) {
            if (s != null) {
                Runtime.getRuntime().exec("taskkill /f /im " + s);
                unkillProcess.getItems().add(s);
            }
        }

        JOptionPane.showMessageDialog(null, "Processes in " + eigenLijst.getValue() + " are killed.");
        System.out.println("\neigenlijst is gekilld");

    }

    @FXML
    public void AddList() {
        TablePosition pos = table.getSelectionModel().getSelectedCells().get(0);

        // de positie stop je in een int
        int row = pos.getRow();

        // omdat onze table alleen maar observablelist waardes neemt moet je de item die je selecteert naar observablelist casten.
        // in de volgende code wordt de waarde van de kollom in item gestopt
        ObservableList item = table.getItems().get(row);

        // deze code is niet nodig maar voor de leuk houden we hem erin want ik ben te lui om te testen zonder die code.
        TableColumn name = pos.getTableColumn();

        // en als laatste string data krijgt de waarde van de geselecteerde waarde
        String geselecteerdeProcess = (String) name.getCellObservableValue(item).getValue();
        
        unkillProcess.getItems().add(geselecteerdeProcess);
        JOptionPane.showMessageDialog(null, "Added " + geselecteerdeProcess + " to list.");

        
    }
    
    //refresh refresht de listview onder het tabblad Processes
    @FXML
    public void refresh(ActionEvent event) throws Exception {
        // methode showprocessen
        showProcess();
    }

    //initialize wordt gecalld als het programma start
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        try {
            showProcess();
            fillCombobox();

        } catch (Exception ex) {
            Logger.getLogger(KillschermController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
