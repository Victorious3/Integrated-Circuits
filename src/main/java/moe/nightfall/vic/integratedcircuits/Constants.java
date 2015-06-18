package moe.nightfall.vic.integratedcircuits;

public class Constants {
	public static final String MOD_ID = "integratedcircuits";
	public static final String MOD_VERSION = "${version}";
	
	// Circuit format versions:
	//  0 = unnspecified old format, assumed to be 0.8r34 compatible.
	//  1 = 0.9r34 development versions, before version control introduction.
	// Current circuit format version:
	public static final int CURRENT_FORMAT_VERSION = 1;

	public static int GATE_RENDER_ID;

	private Constants() {
	}
}
