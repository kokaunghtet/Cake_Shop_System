package com.cakeshopsystem.models;

public class CakeRecipeInstruction {
    private int cakeRecipeInstructionId;
    private int cakeId;
    private String instruction;

    public CakeRecipeInstruction() {
    }

    public CakeRecipeInstruction(int cakeRecipeInstructionId, int cakeId, String instruction) {
        this.cakeRecipeInstructionId = cakeRecipeInstructionId;
        this.cakeId = cakeId;
        this.instruction = instruction;
    }

    public int getCakeRecipeInstructionId() {
        return cakeRecipeInstructionId;
    }

    public void setCakeRecipeInstructionId(int cakeRecipeInstructionId) {
        this.cakeRecipeInstructionId = cakeRecipeInstructionId;
    }

    public int getCakeId() {
        return cakeId;
    }

    public void setCakeId(int cakeId) {
        this.cakeId = cakeId;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    @Override
    public String toString() {
        return "CakeRecipeInstruction{" +
                "cakeRecipeInstructionId=" + cakeRecipeInstructionId +
                ", cakeId=" + cakeId +
                ", instruction='" + instruction + '\'' +
                '}';
    }
}
