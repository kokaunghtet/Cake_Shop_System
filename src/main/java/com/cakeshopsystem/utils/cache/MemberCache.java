package com.cakeshopsystem.utils.cache;

import com.cakeshopsystem.models.Member;
import com.cakeshopsystem.utils.dao.MemberDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;

public class MemberCache {

    private static final ObservableList<Member> membersList = FXCollections.observableArrayList();
    private static final Map<Integer, Member> membersMap = new HashMap<>();
    private static final Map<String, Integer> phoneToIdMap = new HashMap<>();
    private static final Map<Integer, Integer> qualifiedOrderToMemberIdMap = new HashMap<>();

    private MemberCache() {}

    // ===================== Getters =====================

    public static ObservableList<Member> getMembersList() {
        if (membersList.isEmpty()) refreshMembers();
        return membersList;
    }

    public static Map<Integer, Member> getMembersMap() {
        if (membersMap.isEmpty()) refreshMembers();
        return membersMap;
    }

    public static Member getMemberById(int memberId) {
        if (membersMap.isEmpty()) refreshMembers();
        return membersMap.get(memberId);
    }

    public static Member getMemberByPhone(String phone) {
        if (phone == null || phone.isBlank()) return null;

        if (phoneToIdMap.isEmpty()) refreshMembers();
        Integer id = phoneToIdMap.get(normalizePhone(phone));
        return id == null ? null : getMemberById(id);
    }

    public static Member getMemberByQualifiedOrderId(int qualifiedOrderId) {
        if (qualifiedOrderToMemberIdMap.isEmpty()) refreshMembers();
        Integer memberId = qualifiedOrderToMemberIdMap.get(qualifiedOrderId);
        return memberId == null ? null : getMemberById(memberId);
    }

    // ===================== Refresh =====================

    public static void refreshMembers() {
        membersList.clear();
        membersMap.clear();
        phoneToIdMap.clear();
        qualifiedOrderToMemberIdMap.clear();

        for (Member m : MemberDAO.getAllMembers()) {
            cacheMember(m);
        }
    }

    // ===================== CRUD Wrappers =====================

    public static boolean addMember(Member member) {
        if (member == null) return false;

        boolean ok = MemberDAO.insertMember(member);
        if (ok) cacheMember(member);

        return ok;
    }

    public static boolean updateMember(Member member) {
        if (member == null) return false;

        boolean ok = MemberDAO.updateMember(member);
        if (ok) {
            int idx = findIndexById(member.getMemberId());
            if (idx >= 0) membersList.set(idx, member);
            else membersList.add(member);

            membersMap.put(member.getMemberId(), member);
            phoneToIdMap.put(normalizePhone(member.getPhone()), member.getMemberId());
            qualifiedOrderToMemberIdMap.put(member.getQualifiedOrderId(), member.getMemberId());
        }

        return ok;
    }

    public static boolean deleteMember(int memberId) {
        boolean ok = MemberDAO.deleteMember(memberId);
        if (ok) {
            Member removed = membersMap.remove(memberId);
            membersList.removeIf(m -> m.getMemberId() == memberId);

            if (removed != null) {
                phoneToIdMap.remove(normalizePhone(removed.getPhone()));
                qualifiedOrderToMemberIdMap.remove(removed.getQualifiedOrderId());
            } else {
                rebuildSecondaryMaps();
            }
        }
        return ok;
    }

    // ===================== Helpers =====================

    private static void cacheMember(Member member) {
        if (member == null) return;

        int id = member.getMemberId();

        int idx = findIndexById(id);
        if (idx >= 0) membersList.set(idx, member);
        else membersList.add(member);

        membersMap.put(id, member);
        phoneToIdMap.put(normalizePhone(member.getPhone()), id);
        qualifiedOrderToMemberIdMap.put(member.getQualifiedOrderId(), id);
    }

    private static int findIndexById(int memberId) {
        for (int i = 0; i < membersList.size(); i++) {
            if (membersList.get(i).getMemberId() == memberId) return i;
        }
        return -1;
    }

    private static String normalizePhone(String phone) {
        return phone == null ? "" : phone.trim();
    }

    private static void rebuildSecondaryMaps() {
        phoneToIdMap.clear();
        qualifiedOrderToMemberIdMap.clear();

        for (Member m : membersList) {
            phoneToIdMap.put(normalizePhone(m.getPhone()), m.getMemberId());
            qualifiedOrderToMemberIdMap.put(m.getQualifiedOrderId(), m.getMemberId());
        }
    }
}
