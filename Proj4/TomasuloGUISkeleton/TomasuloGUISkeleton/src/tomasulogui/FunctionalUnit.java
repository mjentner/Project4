package tomasulogui;

public abstract class FunctionalUnit {
  PipelineSimulator simulator;
  ReservationStation[] stations = new ReservationStation[2];
  
  public FunctionalUnit(PipelineSimulator sim) {
    simulator = sim;
  }

 
  public void squashAll() {
    //fill in
    stations[0].data1Valid = false;
    stations[1].data2Valid = false;
  }

  public abstract int calculateResult(int station);

  public abstract int getExecCycles();

  // This method should never be called once equivalent
  // methods on the child classes have been implemented
  // It should probably just be deleted eventually
  // Right now, however, the program doesn't compile without it
  public void execCycle(CDB cdb) {
    //start executing, ask for CDB, etc.
    if (cdb.getDataValid() == false) {
//       cdb.setDataValid(true);
//       cdb.setDataTag(stations[0].tag1);
//       cdb.setDataValue(stations[0].data1);
//       cdb.setDataValid(true);
    }
  }



  public boolean acceptIssue(IssuedInst inst) {
  //fill in reservation station (if available) with data from inst
    if (stations[0] == null) {
		stations[0] = new ReservationStation(simulator);
		stations[0].loadInst(inst);
		return true;
    } else if (stations[1] == null) {
        stations[1] = new ReservationStation(simulator);
		stations[1].loadInst(inst);
		return true;
    }
	else {
		return false;
	}
  }

  public boolean full() {
	  return stations[0] != null && stations[1] != null;
  }

}
