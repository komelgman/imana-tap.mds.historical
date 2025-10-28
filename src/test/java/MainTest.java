import com.imanai.tap.Main;
import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void shouldRunMainWithoutException() {
        Main.main(new String[]{});
    }
}