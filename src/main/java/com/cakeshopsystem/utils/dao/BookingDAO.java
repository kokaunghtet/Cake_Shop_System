package com.cakeshopsystem.utils.dao;

import com.cakeshopsystem.models.Booking;
import com.cakeshopsystem.utils.constants.BookingStatus;
import com.cakeshopsystem.utils.constants.CakeShape;
import com.cakeshopsystem.utils.databaseconnection.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class BookingDAO {

    public static class CustomCakeCardRow {
        private final int customCakeBookingId;
        private final int orderId;
        private final Integer bookingId;
        private final LocalDate pickupDate;
        private final BookingStatus status;

        private final int sizeInches;
        private final CakeShape shape;
        private final String flavourName;
        private final String toppingName;

        public CustomCakeCardRow(int customCakeBookingId, int orderId, Integer bookingId,
                                 LocalDate pickupDate, BookingStatus status,
                                 int sizeInches, CakeShape shape, String flavourName, String toppingName) {
            this.customCakeBookingId = customCakeBookingId;
            this.orderId = orderId;
            this.bookingId = bookingId;
            this.pickupDate = pickupDate;
            this.status = status;
            this.sizeInches = sizeInches;
            this.shape = shape;
            this.flavourName = flavourName;
            this.toppingName = toppingName;
        }

        public int getCustomCakeBookingId() { return customCakeBookingId; }
        public int getOrderId() { return orderId; }
        public Integer getBookingId() { return bookingId; }
        public LocalDate getPickupDate() { return pickupDate; }
        public BookingStatus getStatus() { return status; }
        public int getSizeInches() { return sizeInches; }
        public CakeShape getShape() { return shape; }
        public String getFlavourName() { return flavourName; }
        public String getToppingName() { return toppingName; }
    }

    public static class DiyCardRow {
        private final int diyCakeBookingId;
        private final int orderId;
        private final int bookingId;
        private final BookingStatus status;
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final String memberPhone;

        public DiyCardRow(int diyCakeBookingId, int orderId, int bookingId,
                          BookingStatus status, LocalDateTime startTime, LocalDateTime endTime, String memberPhone) {
            this.diyCakeBookingId = diyCakeBookingId;
            this.orderId = orderId;
            this.bookingId = bookingId;
            this.status = status;
            this.startTime = startTime;
            this.endTime = endTime;
            this.memberPhone = memberPhone;
        }

        public int getDiyCakeBookingId() { return diyCakeBookingId; }
        public int getOrderId() { return orderId; }
        public int getBookingId() { return bookingId; }
        public BookingStatus getStatus() { return status; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public String getMemberPhone() {return memberPhone;}
    }

    // =====================================
    // ========== CRUD OPERATIONS ==========
    // =====================================

    // ----------------- Insert -----------------
    public static boolean insertBooking(Booking booking) {
        String sql = "INSERT INTO bookings (booking_date, booking_status) VALUES (?, ?)";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setDate(1, Date.valueOf(booking.getBookingDate()));
            stmt.setString(2, toDbStatus(booking.getBookingStatus()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Inserting booking failed, no rows affected.");

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    booking.setBookingId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Inserting booking failed, no id obtained.");
                }
            }

            return true;

        } catch (SQLException err) {
            System.err.println("Error inserting booking: " + err.getLocalizedMessage());
            return false;
        }
    }

    // ----------------- Update -----------------
    public static boolean updateBooking(Booking booking) {
        String sql = "UPDATE bookings SET booking_date = ?, booking_status = ? WHERE booking_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(booking.getBookingDate()));
            stmt.setString(2, toDbStatus(booking.getBookingStatus()));
            stmt.setInt(3, booking.getBookingId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error updating booking: " + err.getLocalizedMessage());
            return false;
        }
    }

    // ----------------- Delete -----------------
    public static boolean deleteBooking(int bookingId) {
        String sql = "DELETE FROM bookings WHERE booking_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error deleting booking: " + err.getLocalizedMessage());
            return false;
        }
    }

    // -----------------------------
    // Basic reads
    // -----------------------------
    public static ObservableList<Booking> getAllBookings() {
        ObservableList<Booking> bookings = FXCollections.observableArrayList();
        String sql = "SELECT * FROM bookings ORDER BY booking_date DESC";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) bookings.add(mapBooking(rs));
        } catch (SQLException err) {
            System.err.println("Error fetching bookings: " + err.getLocalizedMessage());
        }
        return bookings;
    }

    public static Booking getBookingById(int bookingId) {
        String sql = "SELECT * FROM bookings WHERE booking_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapBooking(rs);
            }
        } catch (SQLException err) {
            System.err.println("Error fetching booking by id: " + err.getLocalizedMessage());
        }
        return null;
    }

    public static ObservableList<Booking> getBookingsByDate(LocalDate bookingDate) {
        ObservableList<Booking> bookings = FXCollections.observableArrayList();
        String sql = "SELECT * FROM bookings WHERE booking_date = ? ORDER BY booking_id DESC";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(bookingDate));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) bookings.add(mapBooking(rs));
            }
        } catch (SQLException err) {
            System.err.println("Error fetching bookings by date: " + err.getLocalizedMessage());
        }
        return bookings;
    }

    // -----------------------------
    // Transaction-safe insert for OrderService
    // -----------------------------
    public static int insertBookingReturnId(Connection con, LocalDate bookingDate, BookingStatus status) throws SQLException {
        String sql = "INSERT INTO bookings (booking_date, booking_status) VALUES (?, ?)";

        if (con == null) throw new SQLException("Connection is required");
        if (bookingDate == null) throw new SQLException("booking_date is required");

        try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setDate(1, Date.valueOf(bookingDate));
            stmt.setString(2, toDbStatus(status));

            int affected = stmt.executeUpdate();
            if (affected != 1) throw new SQLException("Failed to create booking.");

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (!rs.next()) throw new SQLException("Failed to get booking_id.");
                return rs.getInt(1);
            }
        }
    }

    // -----------------------------
    // Status update (public wrapper)
    // -----------------------------
    public static boolean updateBookingStatus(int bookingId, BookingStatus newStatus) {
        if (newStatus == null) return false;

        try (Connection con = DB.connect()) {
            con.setAutoCommit(false);
            try {
                boolean ok = updateBookingStatus(con, bookingId, newStatus);
                con.commit();
                return ok;
            } catch (SQLException ex) {
                con.rollback();
                System.err.println("Error updating booking status: " + ex.getLocalizedMessage());
                return false;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException err) {
            System.err.println("DB error: " + err.getLocalizedMessage());
            return false;
        }
    }

    // =========================================================
    // FULL FIXED METHOD (Option B + keep history)
    // =========================================================
    private static boolean updateBookingStatus(Connection con, int bookingId, BookingStatus newStatus) throws SQLException {
        if (con == null) throw new SQLException("Connection is required");
        if (newStatus == null) return false;

        BookingStatus oldStatus;
        String lockBookingSql = "SELECT booking_status FROM bookings WHERE booking_id = ? FOR UPDATE";
        try (PreparedStatement ps = con.prepareStatement(lockBookingSql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                oldStatus = fromDbStatus(rs.getString("booking_status"));
            }
        }

        if (oldStatus == newStatus) return true;

        boolean oldActive = isActiveStatus(oldStatus);
        boolean newActive = isActiveStatus(newStatus);

        // -------- Custom cake quota --------
        LocalDate pickupDate = null;
        String lockCustomSql = """
            SELECT pickup_date
            FROM custom_cake_bookings
            WHERE booking_id = ?
            LIMIT 1
            FOR UPDATE
            """;
        try (PreparedStatement ps = con.prepareStatement(lockCustomSql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Date d = rs.getDate("pickup_date");
                    pickupDate = (d == null) ? null : d.toLocalDate();
                }
            }
        }

        if (pickupDate != null) {
            if (oldActive && !newActive) {
                releaseCustomCakeSlot(con, pickupDate);
            } else if (!oldActive && newActive) {
                reserveCustomCakeSlot(con, pickupDate, 3);
            }
        }

        // -------- DIY quota (keep history) --------
        LocalDate diyDate = null;
        LocalTime diyStart = null;

        String lockDiySql = """
            SELECT session_date, session_start
            FROM diy_cake_bookings
            WHERE booking_id = ?
            LIMIT 1
            FOR UPDATE
            """;
        try (PreparedStatement ps = con.prepareStatement(lockDiySql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Date d = rs.getDate("session_date");
                    Time t = rs.getTime("session_start");
                    diyDate = (d == null) ? null : d.toLocalDate();
                    diyStart = (t == null) ? null : t.toLocalTime();
                }
            }
        }

        if (diyDate != null && diyStart != null) {
            if (!isAllowedDiySessionStart(diyStart)) {
                throw new SQLException("Invalid DIY session_start: " + diyStart);
            }

            if (oldActive && !newActive) {
                releaseDiySlot(con, diyDate, diyStart);
            } else if (!oldActive && newActive) {
                reserveDiySlot(con, diyDate, diyStart);
            }
        }

        // -------- Update booking status --------
        String updateSql = "UPDATE bookings SET booking_status = ? WHERE booking_id = ?";
        try (PreparedStatement ps = con.prepareStatement(updateSql)) {
            ps.setString(1, toDbStatus(newStatus));
            ps.setInt(2, bookingId);
            return ps.executeUpdate() == 1;
        }
    }

    // -----------------------------
    // Quota helpers (Custom)
    // -----------------------------
    private static boolean isActiveStatus(BookingStatus st) {
        return st == BookingStatus.PENDING || st == BookingStatus.CONFIRMED;
    }

    private static void ensureQuotaRow(Connection con, LocalDate pickupDate) throws SQLException {
        String ensureRow = """
            INSERT INTO custom_cake_daily_quota (pickup_date, booked_count)
            VALUES (?, 0)
            ON DUPLICATE KEY UPDATE booked_count = booked_count
            """;
        try (PreparedStatement ps = con.prepareStatement(ensureRow)) {
            ps.setDate(1, Date.valueOf(pickupDate));
            ps.executeUpdate();
        }
    }

    public static void reserveCustomCakeSlot(Connection con, LocalDate pickupDate, int limitPerDay) throws SQLException {
        if (pickupDate == null) throw new SQLException("Pickup date is required.");

        ensureQuotaRow(con, pickupDate);

        int current;
        String lock = "SELECT booked_count FROM custom_cake_daily_quota WHERE pickup_date = ? FOR UPDATE";
        try (PreparedStatement ps = con.prepareStatement(lock)) {
            ps.setDate(1, Date.valueOf(pickupDate));
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Quota row missing for: " + pickupDate);
                current = rs.getInt(1);
            }
        }

        if (current + 1 > limitPerDay) {
            throw new SQLException("Custom cake limit reached for pickup date: " + pickupDate);
        }

        String inc = "UPDATE custom_cake_daily_quota SET booked_count = booked_count + 1 WHERE pickup_date = ?";
        try (PreparedStatement ps = con.prepareStatement(inc)) {
            ps.setDate(1, Date.valueOf(pickupDate));
            ps.executeUpdate();
        }
    }

    public static void releaseCustomCakeSlot(Connection con, LocalDate pickupDate) throws SQLException {
        if (pickupDate == null) return;

        ensureQuotaRow(con, pickupDate);

        int current;
        String lock = "SELECT booked_count FROM custom_cake_daily_quota WHERE pickup_date = ? FOR UPDATE";
        try (PreparedStatement ps = con.prepareStatement(lock)) {
            ps.setDate(1, Date.valueOf(pickupDate));
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return;
                current = rs.getInt(1);
            }
        }
        if (current <= 0) return;

        String dec = "UPDATE custom_cake_daily_quota SET booked_count = booked_count - 1 WHERE pickup_date = ?";
        try (PreparedStatement ps = con.prepareStatement(dec)) {
            ps.setDate(1, Date.valueOf(pickupDate));
            ps.executeUpdate();
        }
    }

    // -----------------------------
    // Quota helpers (DIY)
    // -----------------------------
    private static boolean isAllowedDiySessionStart(LocalTime t) {
        return t != null && (
                t.equals(LocalTime.of(9, 0)) ||
                        t.equals(LocalTime.of(12, 0)) ||
                        t.equals(LocalTime.of(15, 0))
        );
    }

    private static void ensureDiyQuotaRow(Connection con, LocalDate sessionDate, LocalTime sessionStart) throws SQLException {
        String ensureRow = """
            INSERT INTO diy_session_quota (session_date, session_start, booked_count)
            VALUES (?, ?, 0)
            ON DUPLICATE KEY UPDATE booked_count = booked_count
            """;
        try (PreparedStatement ps = con.prepareStatement(ensureRow)) {
            ps.setDate(1, Date.valueOf(sessionDate));
            ps.setTime(2, Time.valueOf(sessionStart));
            ps.executeUpdate();
        }
    }

    public static void reserveDiySlot(Connection con, LocalDate sessionDate, LocalTime sessionStart) throws SQLException {
        if (sessionDate == null) throw new SQLException("session_date is required.");
        if (sessionStart == null) throw new SQLException("session_start is required.");
        if (!isAllowedDiySessionStart(sessionStart)) throw new SQLException("Invalid diy session_start.");

        ensureDiyQuotaRow(con, sessionDate, sessionStart);

        int current;
        String lock = """
            SELECT booked_count
            FROM diy_session_quota
            WHERE session_date = ? AND session_start = ?
            FOR UPDATE
            """;
        try (PreparedStatement ps = con.prepareStatement(lock)) {
            ps.setDate(1, Date.valueOf(sessionDate));
            ps.setTime(2, Time.valueOf(sessionStart));
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("DIY quota row missing.");
                current = rs.getInt(1);
            }
        }

        if (current + 1 > 1) throw new SQLException("DIY slot already booked: " + sessionDate + " " + sessionStart);

        String inc = """
            UPDATE diy_session_quota
            SET booked_count = booked_count + 1
            WHERE session_date = ? AND session_start = ?
            """;
        try (PreparedStatement ps = con.prepareStatement(inc)) {
            ps.setDate(1, Date.valueOf(sessionDate));
            ps.setTime(2, Time.valueOf(sessionStart));
            ps.executeUpdate();
        }
    }

    public static void releaseDiySlot(Connection con, LocalDate sessionDate, LocalTime sessionStart) throws SQLException {
        if (sessionDate == null || sessionStart == null) return;
        if (!isAllowedDiySessionStart(sessionStart)) return;

        ensureDiyQuotaRow(con, sessionDate, sessionStart);

        String lock = """
            SELECT booked_count
            FROM diy_session_quota
            WHERE session_date = ? AND session_start = ?
            FOR UPDATE
            """;
        try (PreparedStatement ps = con.prepareStatement(lock)) {
            ps.setDate(1, Date.valueOf(sessionDate));
            ps.setTime(2, Time.valueOf(sessionStart));
            ps.executeQuery(); // lock row
        }

        String dec = """
            UPDATE diy_session_quota
            SET booked_count = GREATEST(booked_count - 1, 0)
            WHERE session_date = ? AND session_start = ?
            """;
        try (PreparedStatement ps = con.prepareStatement(dec)) {
            ps.setDate(1, Date.valueOf(sessionDate));
            ps.setTime(2, Time.valueOf(sessionStart));
            ps.executeUpdate();
        }
    }

    // -----------------------------
    // Card queries (your UI)
    // -----------------------------
    public static ObservableList<CustomCakeCardRow> getCustomCakeCards() {
        ObservableList<CustomCakeCardRow> rows = FXCollections.observableArrayList();

        String sql = """
            SELECT
              ccb.custom_cake_booking_id,
              ccb.order_id,
              ccb.booking_id,
              ccb.pickup_date,
              COALESCE(b.booking_status, 'Pending') AS booking_status,
              s.size_inches,
              ccb.shape,
              f.flavour_name,
              t.topping_name
            FROM custom_cake_bookings ccb
            LEFT JOIN bookings b ON b.booking_id = ccb.booking_id
            JOIN sizes s ON s.size_id = ccb.size_id
            JOIN flavours f ON f.flavour_id = ccb.flavour_id
            LEFT JOIN toppings t ON t.topping_id = ccb.topping_id
            WHERE ccb.pickup_date >= CURRENT_DATE
            ORDER BY ccb.pickup_date ASC, ccb.custom_cake_booking_id DESC
            """;

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("custom_cake_booking_id");
                int orderId = rs.getInt("order_id");
                Integer bookingId = (Integer) rs.getObject("booking_id");

                Date d = rs.getDate("pickup_date");
                LocalDate pickupDate = (d == null) ? null : d.toLocalDate();

                BookingStatus status = fromDbStatus(rs.getString("booking_status"));

                int sizeInches = rs.getInt("size_inches");
                CakeShape shape = fromDbShape(rs.getString("shape"));

                String flavourName = rs.getString("flavour_name");
                String toppingName = rs.getString("topping_name");

                rows.add(new CustomCakeCardRow(id, orderId, bookingId, pickupDate, status, sizeInches, shape, flavourName, toppingName));
            }

        } catch (SQLException err) {
            System.err.println("Error fetching custom cake cards: " + err.getLocalizedMessage());
        }

        return rows;
    }

    public static ObservableList<DiyCardRow> getDiyBookingCards() {
        ObservableList<DiyCardRow> rows = FXCollections.observableArrayList();

        String sql = """
        SELECT
          dcb.diy_cake_booking_id,
          dcb.order_id,
          dcb.booking_id,
          b.booking_status,
          dcb.session_date,
          dcb.session_start,
          m.phone AS member_phone
        FROM diy_cake_bookings dcb
        JOIN bookings b ON b.booking_id = dcb.booking_id
        JOIN members  m ON m.member_id  = dcb.member_id
        WHERE dcb.session_date >= CURRENT_DATE
        ORDER BY dcb.session_date ASC, dcb.session_start ASC, dcb.diy_cake_booking_id DESC
        """;

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("diy_cake_booking_id");
                int orderId = rs.getInt("order_id");
                int bookingId = rs.getInt("booking_id");

                BookingStatus status = fromDbStatus(rs.getString("booking_status"));

                Date d = rs.getDate("session_date");
                Time t = rs.getTime("session_start");

                LocalDateTime start = (d == null || t == null) ? null
                        : LocalDateTime.of(d.toLocalDate(), t.toLocalTime());

                LocalDateTime end = (start == null) ? null : start.plusHours(2);

                String phone = rs.getString("member_phone");

                rows.add(new DiyCardRow(id, orderId, bookingId, status, start, end, phone));
            }

        } catch (SQLException err) {
            System.err.println("Error fetching DIY booking cards: " + err.getLocalizedMessage());
        }

        return rows;
    }


    // -----------------------------
    // Mappers
    // -----------------------------
    private static Booking mapBooking(ResultSet rs) throws SQLException {
        int bookingId = rs.getInt("booking_id");

        Date d = rs.getDate("booking_date");
        LocalDate bookingDate = (d == null) ? null : d.toLocalDate();

        BookingStatus status = fromDbStatus(rs.getString("booking_status"));
        return new Booking(bookingId, bookingDate, status);
    }

    private static BookingStatus fromDbStatus(String dbValue) {
        if (dbValue == null) return BookingStatus.PENDING;

        String s = dbValue.trim();
        if (s.isEmpty()) return BookingStatus.PENDING;

        String upper = s.toUpperCase();
        if ("PENDING".equals(upper)) return BookingStatus.PENDING;
        if ("CONFIRMED".equals(upper)) return BookingStatus.CONFIRMED;
        if ("CANCELLED".equals(upper) || "CANCELED".equals(upper)) return BookingStatus.CANCELLED;

        for (BookingStatus st : BookingStatus.values()) {
            if (st.name().equalsIgnoreCase(s)) return st;
            if (st.toString().equalsIgnoreCase(s)) return st;
        }
        return BookingStatus.PENDING;
    }

    private static String toDbStatus(BookingStatus status) {
        if (status == null) return "Pending";
        return switch (status) {
            case PENDING -> "Pending";
            case CONFIRMED -> "Confirmed";
            case CANCELLED -> "Cancelled";
        };
    }

    private static CakeShape fromDbShape(String dbValue) {
        if (dbValue == null) return null;

        String s = dbValue.trim();
        if (s.isEmpty()) return null;

        String upper = s.toUpperCase();
        if ("SQUARE".equals(upper)) return CakeShape.SQUARE;
        if ("CIRCLE".equals(upper)) return CakeShape.CIRCLE;
        if ("HEART".equals(upper)) return CakeShape.HEART;

        for (CakeShape sh : CakeShape.values()) {
            if (sh.name().equalsIgnoreCase(s)) return sh;
            if (sh.toString().equalsIgnoreCase(s)) return sh;
        }

        return null;
    }
}
