/**
 * Created by Student Name: Saumya Pandey.
 */
import org.kohsuke.args4j.Option;

public class ServerCmdLineArgs {

    @Option(name="-p",usage="Port Num")
    private int port;

    @Option(name="-f",usage="Path")
    private String path;

    public int getPort() {
        return port;
    }

    public String getFile() {
        return path;
    }
}
