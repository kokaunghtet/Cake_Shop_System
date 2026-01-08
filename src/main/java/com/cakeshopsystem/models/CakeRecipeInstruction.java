package com.cakeshopsystem.models;

public class CakeRecipeInstruction {
    private int cakeRecipeInstructionId;
    private String instruction;

    public CakeRecipeInstruction() {
    }

    public CakeRecipeInstruction(int cakeRecipeInstructionId, String instruction) {
        this.cakeRecipeInstructionId = cakeRecipeInstructionId;
        this.instruction = instruction;
    }

    public int getCakeRecipeInstructionId() {
        return cakeRecipeInstructionId;
    }

    public void setCakeRecipeInstructionId(int cakeRecipeInstructionId) {
        this.cakeRecipeInstructionId = cakeRecipeInstructionId;
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
                ", instruction='" + instruction + '\'' +
                '}';
    }
}
