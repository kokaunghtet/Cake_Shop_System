package com.cakeshopsystem.utils.dao;

import com.cakeshopsystem.models.Member;
import com.cakeshopsystem.utils.databaseconnection.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime;

public class MemberDAO {

    public static ObservableList<Member> getAllMembers() {
        ObservableList<Member> members = FXCollections.observableArrayList();
        String query = "SELECT member_id, member_name, phone, member_since, qualified_order_id FROM members";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                members.add(mapMember(rs));
            }

        } catch (SQLException err) {
            System.err.println("Error fetching members: " + err.getLocalizedMessage());
        }

        return members;
    }

    public static Member getMemberById(int memberId) {
        String query = "SELECT member_id, member_name, phone, member_since, qualified_order_id FROM members WHERE member_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, memberId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapMember(rs);
            }

        } catch (SQLException err) {
            System.err.println("Error fetching member by id: " + err.getLocalizedMessage());
        }

        return null;
    }

    public static Member getMemberByPhone(String phone) {
        if (phone == null || phone.isBlank()) return null;

        String query = "SELECT member_id, member_name, phone, member_since, qualified_order_id FROM members WHERE phone = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setString(1, phone.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapMember(rs);
            }

        } catch (SQLException err) {
            System.err.println("Error fetching member by phone: " + err.getLocalizedMessage());
        }

        return null;
    }

    public static Member getMemberByQualifiedOrderId(int qualifiedOrderId) {
        String query = "SELECT member_id, member_name, phone, member_since, qualified_order_id FROM members WHERE qualified_order_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, qualifiedOrderId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapMember(rs);
            }

        } catch (SQLException err) {
            System.err.println("Error fetching member by qualified order id: " + err.getLocalizedMessage());
        }

        return null;
    }

    // =====================================
    // ========== CRUD OPERATIONS ==========
    // =====================================

    public static boolean insertMember(Member member) {
        String sql = "INSERT INTO members (member_name, phone, member_since, qualified_order_id) VALUES (?, ?, ?, ?)";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, member.getMemberName());
            stmt.setString(2, member.getPhone());

            LocalDateTime since = member.getMemberSince();
            if (since == null) since = LocalDateTime.now();
            stmt.setTimestamp(3, Timestamp.valueOf(since));

            stmt.setInt(4, member.getQualifiedOrderId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Inserting member failed, no rows affected.");

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    member.setMemberId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Inserting member failed, no id obtained.");
                }
            }

            return true;

        } catch (SQLException err) {
            System.err.println("Error inserting member: " + err.getLocalizedMessage());
            return false;
        }
    }

    public static boolean updateMember(Member member) {
        // Keep this simple: update name + phone only (qualified_order_id is usually “fixed”)
        String sql = "UPDATE members SET member_name = ?, phone = ? WHERE member_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, member.getMemberName());
            stmt.setString(2, member.getPhone());
            stmt.setInt(3, member.getMemberId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error updating member: " + err.getLocalizedMessage());
            return false;
        }
    }

    public static boolean deleteMember(int memberId) {
        String sql = "DELETE FROM members WHERE member_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, memberId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error deleting member: " + err.getLocalizedMessage());
            return false;
        }
    }

    // =====================================
    // ========== Helper Methods ===========
    // =====================================

    private static Member mapMember(ResultSet rs) throws SQLException {
        int memberId = rs.getInt("member_id");
        String memberName = rs.getString("member_name");
        String phone = rs.getString("phone");

        Timestamp ts = rs.getTimestamp("member_since");
        LocalDateTime memberSince = (ts == null) ? null : ts.toLocalDateTime();

        int qualifiedOrderId = rs.getInt("qualified_order_id");

        return new Member(memberId, memberName, phone, memberSince, qualifiedOrderId);
    }
}
