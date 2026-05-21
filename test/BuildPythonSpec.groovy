import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.*

class BuildPythonSpec extends BasePipelineTest {

    def shCalls = []
    def echoCalls = []

    @BeforeEach
    void setUp() {
        super.setUp()
        helper.registerAllowedMethod('sh', [String.class], { cmd ->
            shCalls << cmd.trim()
        })
        helper.registerAllowedMethod('echo', [String.class], { msg ->
            echoCalls << msg
        })
    }

    @Test
    void testDefaultConfig() {
        def script = loadScript('vars/buildPython.groovy')
        script.call([:])

        assertTrue(shCalls.any { it.contains('python3.11') }, 'should install with python3.11')
        assertTrue(shCalls.any { it.contains('flake8') }, 'should run flake8 lint')
        assertTrue(shCalls.any { it.contains('pytest') }, 'should run pytest')
        assertTrue(shCalls.any { it.contains('--cov-fail-under=80') }, 'should enforce 80% coverage')
        assertTrue(echoCalls.any { it.contains('coverage.xml') }, 'should echo coverage report path')
    }

    @Test
    void testCustomConfig() {
        def script = loadScript('vars/buildPython.groovy')
        script.call([
            pythonVersion: '3.12',
            requirementsFile: 'requirements-dev.txt',
            testDir: 'src/tests/',
            coverageThreshold: 90
        ])

        assertTrue(shCalls.any { it.contains('python3.12') }, 'should use python3.12')
        assertTrue(shCalls.any { it.contains('requirements-dev.txt') }, 'should install custom requirements')
        assertTrue(shCalls.any { it.contains('src/tests/') }, 'should point pytest at custom testDir')
        assertTrue(shCalls.any { it.contains('--cov-fail-under=90') }, 'should enforce 90% coverage threshold')
    }
}
