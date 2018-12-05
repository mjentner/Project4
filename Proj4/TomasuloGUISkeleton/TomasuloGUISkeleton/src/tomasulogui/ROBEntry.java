package tomasulogui;

public class ROBEntry {
  ReorderBuffer rob;

  // TODO - add many more fields into entry
  // I deleted most, and only kept those necessary to compile GUI
  boolean complete = false;
  boolean predictTaken = false;
  boolean mispredicted = false;
  int target = -1;
  int instPC = -1;
  int writeReg = -1;
  int writeValue = -1;
  int writeAddress = -1;

  IssuedInst.INST_TYPE opcode;

  public ROBEntry(ReorderBuffer buffer) {
    rob = buffer;
  }

  public boolean isComplete() {
    return complete;
  }

  public boolean branchMispredicted() {
    return mispredicted;
  }

  public boolean getPredictTaken() {
    return predictTaken;
  }

  public int getInstPC() {
    return instPC;
  }
  
  public int getTarget() {
      return target;
  }

  public IssuedInst.INST_TYPE getOpcode () {
    return opcode;
  }

//  public boolean isRegDest() {
//      return opcode == IssuedInst.INST_TYPE.ADD 
//          || opcode == IssuedInst.INST_TYPE.ADDI
//          || opcode == IssuedInst.INST_TYPE.SUB
//          || opcode == IssuedInst.INST_TYPE.MUL
//          || opcode == IssuedInst.INST_TYPE.DIV
//          || opcode == IssuedInst.INST_TYPE.AND
//          || opcode == IssuedInst.INST_TYPE.ANDI
//          || opcode == IssuedInst.INST_TYPE.OR
//          || opcode == IssuedInst.INST_TYPE.ORI
//          || opcode == IssuedInst.INST_TYPE.XOR
//          || opcode == IssuedInst.INST_TYPE.XORI
//          || opcode == IssuedInst.INST_TYPE.SLL
//          || opcode == IssuedInst.INST_TYPE.SRL
//          || opcode == IssuedInst.INST_TYPE.SRA
//          || opcode == IssuedInst.INST_TYPE.LOAD
//          || opcode == IssuedInst.INST_TYPE.NOP;
//  }
  
//  public boolean isStore() {
//      return opcode == IssuedInst.INST_TYPE.STORE;
//  }
  
//  public boolean isBranch() {
//      return opcode == IssuedInst.INST_TYPE.BEQ
//          || opcode == IssuedInst.INST_TYPE.BNE
//          || opcode == IssuedInst.INST_TYPE.BLTZ
//          || opcode == IssuedInst.INST_TYPE.BLEZ
//          || opcode == IssuedInst.INST_TYPE.BGEZ
//          || opcode == IssuedInst.INST_TYPE.BGTZ
//          || opcode == IssuedInst.INST_TYPE.J
//          || opcode == IssuedInst.INST_TYPE.JAL
//          || opcode == IssuedInst.INST_TYPE.JR
//          || opcode == IssuedInst.INST_TYPE.JALR;
//  }

  public boolean isHaltOpcode() {
    return (opcode == IssuedInst.INST_TYPE.HALT);
  }

  public void setBranchTaken(boolean result) {
  // TODO - maybe more than simple set
  }
  
  public int getWriteAddress() {
      return writeAddress;
  }

  public int getWriteReg() {
    return writeReg;
  }

  public int getWriteValue() {
    return writeValue;
  }

  public void setWriteValue(int value) {
    writeValue = value;
  }

  public void copyInstData(IssuedInst inst, int frontQ) {
    instPC = inst.getPC();
    inst.setRegDestTag(frontQ);

    // TODO - This is a long and complicated method, probably the most complex
    // of the project.  It does 2 things:
    // 1. update the instruction, as shown in 2nd line of code above
    // 2. update the fields of the ROBEntry, as shown in the 1st line of code above

  }

}
