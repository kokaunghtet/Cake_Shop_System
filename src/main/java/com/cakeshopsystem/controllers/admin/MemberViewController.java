package com.cakeshopsystem.controllers.admin;

import com.cakeshopsystem.models.Member;
import com.cakeshopsystem.utils.cache.MemberCache;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class MemberViewController {

    @FXML
    private TableColumn<Member, String> colName;

    @FXML
    private TableColumn<Member, String> colPhoneNumber;

    @FXML
    private TableView<Member> memberTableView;

    @FXML
    private TextField txtSearch;

    public void initialize() {
        // Initialization code for MemberViewController

        ObservableList<Member> memberList = MemberCache.getMembersList();
        memberTableView.setItems(memberList);

        configureTable();
    }

    private void configureTable() {
        memberTableView.setFixedCellSize(50);

        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMemberName()));
        colPhoneNumber.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPhone()));
    }
}