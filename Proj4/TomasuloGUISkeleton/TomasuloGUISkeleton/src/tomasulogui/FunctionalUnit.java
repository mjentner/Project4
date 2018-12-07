package tomasulogui;

public abstract class FunctionalUnit {
	public static final int BUFFER_SIZE = 2;
	PipelineSimulator simulator;
	ReservationStation[] stations = new ReservationStation[2];
	boolean canWriteback = false;
	int writebackStation = -1;
	boolean requestWriteback = false;
	int executionStage = -1;
	int writeData = -1;
	int writeTag = -1;

	public boolean isRequestingWriteback() {
		return requestWriteback;
	}

	public void setCanWriteback() {
		canWriteback = true;
	}

	public int getWriteTag() {
		return writeTag;
	}

	public int getWriteData() {
		return writeData;
	}
  
  public FunctionalUnit(PipelineSimulator sim) {
    simulator = sim;
  }

 
  public void squashAll() {
    //fill in
//	executionProgress = -1;
//	stationInUse = -1;
	stations[0] = null;
	stations[1] = null;
  }

  public abstract int calculateResult(int station);

  public abstract int getExecCycles();

  public void execCycle(CDB cdb) {
    // first check if a reservation station was freed by writeback
    if (canWriteback) {
      stations[writebackStation] = null;
      writebackStation = -1;
      requestWriteback = false;
	  executionStage = -1;
    }
    // only execute if not stuck
    if (!requestWriteback) {
		if (executionStage == -1) {
			for (int i = 0; i < BUFFER_SIZE; i++) {
				ReservationStation station = stations[i];
				if (station != null && station.isReady()) {
					writebackStation = i;
					writeTag = station.getDestTag();
					executionStage = 1;
					break;
				}
			}
		}
		else {
			executionStage++;
		}
		if (executionStage == getExecCycles()) {
			
			// we are finished execution

			requestWriteback = true;
			writeData = calculateResult(writebackStation);
		}

    }
    // check reservationStations for cdb data
    if (cdb.getDataValid()) {
      for (int i = 0; i < BUFFER_SIZE; i++) {
        if (stations[i] != null) {
          stations[i].snoop(cdb);
        }
      }
    }
	canWriteback = false;
  }

  public void acceptIssue(IssuedInst inst) {
  //fill in reservation station (if available) with data from inst
    int slot=0;
    for (slot=0; slot < BUFFER_SIZE; slot++) {
     if (stations[slot] == null) {
       break;
     }
   }
   if (slot == BUFFER_SIZE) {
     throw new MIPSException("Functional Unit accept issue: station not available");
   }

   ReservationStation station = new ReservationStation(simulator);
   stations[slot] = station;

   station.loadInst (inst);
  }

  public boolean isReservationStationAvail() {
	  return stations[0] == null || stations[1] == null;
  }

}
