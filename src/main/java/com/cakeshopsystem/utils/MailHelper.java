package com.cakeshopsystem.utils;

import com.cakeshopsystem.models.OTP;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.SnackBarType;
import com.cakeshopsystem.utils.dao.OneTimePasswordDAO;
import com.cakeshopsystem.utils.dao.UserDAO;
import com.cakeshopsystem.utils.mail.GmailSMTP;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.util.Duration;

import java.util.concurrent.CompletableFuture;

public class MailHelper {

    public static CompletableFuture<MailResultSet> sendMail(String targetMail, String mailBodyText) {
        CompletableFuture<MailResultSet> future = new CompletableFuture<>();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    int userId = UserDAO.isEmailExists(targetMail);

                    if (userId <= 0) {
                        future.complete(new MailResultSet(true, "If an account exists, an OTP will be sent."));
                        return null;
                    }

                    OTP otp = new OTP(userId);
                    OTPResult otpResult = OneTimePasswordDAO.insertOTP(userId, otp);

                    if (!otpResult.success()) {
                        future.complete(new MailResultSet(false, otpResult.message()));
                        return null;
                    }

                    String body = mailBodyText + "\nYour OTP code is " + otp.getCode() + " (valid 5 minutes).";

                    boolean sent = GmailSMTP.sendMail(targetMail, body);

                    future.complete(sent
                            ? new MailResultSet(true, "Mail sent successfully")
                            : new MailResultSet(false, "Mail could not be sent"));

                } catch (Exception ex) {
                    ex.printStackTrace(); // IMPORTANT while debugging
                    future.complete(new MailResultSet(false, "System error: " + ex.getMessage()));
                }
                return null;
            }
        };

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();

        return future;
    }
//
//
//    public static CompletableFuture<MailResultSet> sendMail(String targetMail, String mailBodyText) {
//        CompletableFuture<MailResultSet> future = new CompletableFuture<>();
//
//        Task<Void> task = new Task<Void>() {
//            @Override
//            protected Void call() {
//                String mailBody = generateMailBody(targetMail, mailBodyText);
//
//                if (mailBody == null) {
//                    Platform.runLater(() -> {
//                        future.complete(new MailResultSet(false, "Mail cannot be sent: not registered"));
//                    });
//                    return null;
//                }
//
//                boolean isMessageSent = GmailSMTP.sendMail(targetMail, mailBody);
//
//                Platform.runLater(() -> {
//                    if (isMessageSent) {
//                        future.complete(new MailResultSet(true, "Mail sent successful"));
//                    } else {
//                        future.complete(new MailResultSet(false, "Mail could not be sent"));
//                    }
//                });
//
//                return null;
//            }
//        };
//
//        new Thread(task).start();
//        return future;
//    }
//
//    public static String generateMailBody(String targetMail, String mailBody) {
//        int user_id = UserDAO.isEmailExists(targetMail);
//        if (user_id > 0) {
//            OTP otp = new OTP(user_id);
//            OTPResult otpResult = OneTimePasswordDAO.insertOTP(user_id, otp);
//
//            if (otpResult.success()) {
//                return mailBody + "\nYour OTP code is " + otp.getCode() + " and will be available for 5 minutes.";
//            }
//            SnackBar.show(SnackBarType.SUCCESS, "OTP was sent successfully", "", Duration.seconds(3));
//        }
//        return null;
//    }
}
