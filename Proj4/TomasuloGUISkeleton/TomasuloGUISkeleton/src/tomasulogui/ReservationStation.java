package tomasulogui;

public class ReservationStation {
  PipelineSimulator simulator;

  int tag1;
  int tag2;
  int data1;
  int data2;
  boolean data1Valid = false;
  boolean data2Valid = false;
  boolean available = true;
  // destTag doubles as branch tag
  int destTag;
  IssuedInst.INST_TYPE function = IssuedInst.INST_TYPE.NOP;

  // following just for branches
  int addressTag;
  boolean addressValid = false;
  int address;
  boolean predictedTaken = false;

  public ReservationStation(PipelineSimulator sim) {
    simulator = sim;
  }

  public int getDestTag() {
    return destTag;
  }

  public boolean isAvailable() {
	  return available;
  }
  
  public int getData1() {
    return data1;
  }

  public int getData2() {
    return data2;
  }

  public boolean isPredictedTaken() {
    return predictedTaken;
  }

  public IssuedInst.INST_TYPE getFunction() {
    return function;
  }

  public void snoop(CDB cdb) {
    //code to snoop on CDB each cycle
    if (cdb.dataTag == tag1) {
        data1 = cdb.dataValue;
        data1Valid = cdb.dataValid;
    } else if (cdb.dataTag == tag2) {
        data2 = cdb.dataValue;
        data2Valid = cdb.dataValid;
    }
  }

  public boolean isReady() {
    return data1Valid && data2Valid;
  }

  public void loadInst(IssuedInst inst) {
    // TODO add code to insert inst into reservation station
    destTag = inst.regDestTag;
    function = inst.opcode;
    address = inst.branchTgt;
  }
}
