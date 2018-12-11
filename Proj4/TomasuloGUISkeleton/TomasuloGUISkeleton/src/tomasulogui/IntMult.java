package tomasulogui;

public class IntMult extends FunctionalUnit {

    public static final int EXEC_CYCLES = 4;

    public IntMult(PipelineSimulator sim) {
        super(sim);
    }

	@Override
    public int calculateResult(int station) {
		ReservationStation stat = stations[station];
        return stat.getData1() * stat.getData2();
    }

	@Override
    public int getExecCycles() {
        return EXEC_CYCLES;
    }
}
