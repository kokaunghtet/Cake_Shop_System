package com.cakeshopsystem.models;

import java.time.LocalDateTime;

public class Member {
    private int memberId;
    private String memberName;
    private String phone;
    private LocalDateTime memberSince = LocalDateTime.now();
    private int qualifiedOrderId;

    public Member() {
    }

    public Member(int memberId, String memberName, String phone, LocalDateTime memberSince, int qualifiedOrderId) {
        this.memberId = memberId;
        this.memberName = memberName;
        this.phone = phone;
        this.memberSince = memberSince;
        this.qualifiedOrderId = qualifiedOrderId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDateTime getMemberSince() {
        return memberSince;
    }

    public void setMemberSince(LocalDateTime memberSince) {
        this.memberSince = memberSince;
    }

    public int getQualifiedOrderId() {
        return qualifiedOrderId;
    }

    public void setQualifiedOrderId(int qualifiedOrderId) {
        this.qualifiedOrderId = qualifiedOrderId;
    }

    @Override
    public String toString() {
        return "Member{" +
                "memberId=" + memberId +
                ", memberName='" + memberName + '\'' +
                ", phone='" + phone + '\'' +
                ", memberSince=" + memberSince +
                ", qualifiedOrderId=" + qualifiedOrderId +
                '}';
    }
}
