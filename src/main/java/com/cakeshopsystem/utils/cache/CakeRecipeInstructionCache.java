package com.cakeshopsystem.utils.cache;

import com.cakeshopsystem.models.CakeRecipeInstruction;
import com.cakeshopsystem.utils.dao.CakeRecipeInstructionDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;

public class CakeRecipeInstructionCache {

    private static final ObservableList<CakeRecipeInstruction> list = FXCollections.observableArrayList();
    private static final Map<Integer, CakeRecipeInstruction> byId = new HashMap<>();
    private static final Map<Integer, CakeRecipeInstruction> byCakeId = new HashMap<>();

    private CakeRecipeInstructionCache() {}

    public static ObservableList<CakeRecipeInstruction> getList() {
        if (list.isEmpty()) refresh();
        return list;
    }

    public static CakeRecipeInstruction getById(int id) {
        if (byId.isEmpty()) refresh();
        return byId.get(id);
    }

    public static CakeRecipeInstruction getByCakeId(int cakeId) {
        if (byCakeId.isEmpty()) refresh();
        return byCakeId.get(cakeId);
    }

    public static void refresh() {
        list.clear();
        byId.clear();
        byCakeId.clear();

        for (CakeRecipeInstruction cri : CakeRecipeInstructionDAO.getAllCakeRecipeInstructions()) {
            cache(cri);
        }
    }

    public static boolean add(CakeRecipeInstruction cri) {
        if (cri == null) return false;

        boolean ok = CakeRecipeInstructionDAO.insertCakeRecipeInstruction(cri);
        if (ok) cache(cri);

        return ok;
    }

    public static boolean update(CakeRecipeInstruction cri) {
        if (cri == null) return false;

        boolean ok = CakeRecipeInstructionDAO.updateCakeRecipeInstruction(cri);
        if (ok) cache(cri);

        return ok;
    }

    public static boolean delete(int id) {
        boolean ok = CakeRecipeInstructionDAO.deleteCakeRecipeInstruction(id);
        if (ok) {
            CakeRecipeInstruction removed = byId.remove(id);
            if (removed != null) {
                byCakeId.remove(removed.getCakeId());
                list.removeIf(x -> x.getCakeRecipeInstructionId() == id);
            } else {
                // fallback cleanup
                list.removeIf(x -> x.getCakeRecipeInstructionId() == id);
                rebuildMapsFromList();
            }
        }
        return ok;
    }

    // ===================== Helpers =====================

    private static void cache(CakeRecipeInstruction cri) {
        if (cri == null) return;

        int id = cri.getCakeRecipeInstructionId();
        int cakeId = cri.getCakeId();

        int idx = findIndexById(id);
        if (idx >= 0) list.set(idx, cri);
        else list.add(cri);

        byId.put(id, cri);
        byCakeId.put(cakeId, cri);
    }

    private static int findIndexById(int id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getCakeRecipeInstructionId() == id) return i;
        }
        return -1;
    }

    private static void rebuildMapsFromList() {
        byId.clear();
        byCakeId.clear();
        for (CakeRecipeInstruction cri : list) {
            byId.put(cri.getCakeRecipeInstructionId(), cri);
            byCakeId.put(cri.getCakeId(), cri);
        }
    }
}
