package com.cakeshopsystem.controllers.admin;

import com.cakeshopsystem.models.Member;
import com.cakeshopsystem.utils.cache.MemberCache;
import com.cakeshopsystem.utils.session.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.SimpleStringProperty;

public class MemberViewController {

    @FXML
    private TableColumn<Member, String> colName;

    @FXML
    private TableColumn<Member, String> colPhoneNumber;

    @FXML
    private TableColumn<Member, String> colMemberSince;

    @FXML
    private TableView<Member> memberTableView;

    @FXML
    private TextField txtSearch;

    private FilteredList<Member> filteredMembers;
    private SortedList<Member> sortedMembers;


    private static final DateTimeFormatter MEMBER_SINCE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void initialize() {
        ObservableList<Member> memberList = MemberCache.getMembersList();

        filteredMembers = new FilteredList<>(memberList, m -> true);
        sortedMembers = new SortedList<>(filteredMembers);
        sortedMembers.comparatorProperty().bind(memberTableView.comparatorProperty());

        memberTableView.setItems(sortedMembers);

        configureTable();
        configureSearch();

        applySearch();
    }

    private void configureTable() {
        memberTableView.setFixedCellSize(SessionManager.TABLE_CELL_SIZE);

        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMemberName()));
        colPhoneNumber.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPhone()));
        colMemberSince.setCellValueFactory(d -> {
            LocalDateTime dt = d.getValue().getMemberSince();
            return new SimpleStringProperty(dt == null ? "" : dt.format(MEMBER_SINCE_FMT));
        });
    }

    private void configureSearch() {
        txtSearch.textProperty().addListener((obs, old, val) -> applySearch());
    }

    private void applySearch() {
        final String q = safeLower(txtSearch.getText()).trim();

        filteredMembers.setPredicate(m -> {
            if (m == null) return false;
            if (q.isEmpty()) return true;

            return safeLower(m.getMemberName()).contains(q)
                    || safeLower(m.getPhone()).contains(q);
        });
    }

    private String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }
}