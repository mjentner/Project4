package tomasulogui;

public abstract class FunctionalUnit {
  PipelineSimulator simulator;
  ReservationStation[] stations = new ReservationStation[2];
  
  public FunctionalUnit(PipelineSimulator sim) {
    simulator = sim;
    stations[0] = new ReservationStation(sim);
    stations[1] = new ReservationStation(sim);
  }

 
  public void squashAll() {
    //fill in
    stations[0].data1Valid = false;
    stations[1].data2Valid = false;
  }

  public abstract int calculateResult(int station);

  public abstract int getExecCycles();

  public void execCycle(CDB cdb) {
    //start executing, ask for CDB, etc.
    if (cdb.getDataValid() == false) {
       cdb.setDataValid(true);
       cdb.setDataTag(stations[0].tag1);
       cdb.setDataValue(stations[0].data1);
       cdb.setDataValid(true);
    }
  }



  public boolean acceptIssue(IssuedInst inst) {
  //fill in reservation station (if available) with data from inst
    if (stations[0].isAvailable()) {
          stations[0].loadInst(inst);
		  return true;
    } else if (stations[1].isAvailable()) {
        stations[1].loadInst(inst);
		return true;
    }
	else {
		return false;
	}
  }

  public boolean full() {
	  return !(stations[0].isAvailable() || stations[1].isAvailable());
  }

}
