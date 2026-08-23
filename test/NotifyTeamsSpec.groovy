import com.lesfurets.jenkins.unit.BasePipelineTest
import groovy.json.JsonSlurper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.*

class NotifyTeamsSpec extends BasePipelineTest {

    def shCalls = []
    def writtenFiles = [:]

    @BeforeEach
    void setUp() {
        super.setUp()
        binding.setVariable('env', [
            BUILD_URL   : 'https://ci.example.com/job/1',
            JOB_NAME    : 'my-job',
            BUILD_NUMBER: '1',
            GIT_BRANCH  : 'main',
            GIT_COMMIT  : 'abcdef1234567890'
        ])
        helper.registerAllowedMethod('sh', [String.class], { cmd -> shCalls << cmd.trim() })
        helper.registerAllowedMethod('withCredentials', [List.class, Closure.class], { creds, body ->
            binding.setVariable('TEAMS_WEBHOOK', 'https://outlook.office.test/x')
            body()
        })
        helper.registerAllowedMethod('string', [Map.class], { it })
        helper.registerAllowedMethod('writeFile', [Map.class], { args -> writtenFiles[args.file] = args.text })
    }

    @Test
    void testDefaultConfig() {
        def script = loadScript('vars/notifyTeams.groovy')
        script.call([:])

        def payload = new JsonSlurper().parseText(writtenFiles['teams-payload.json'])
        assertEquals('MessageCard', payload['@type'])
        assertEquals('808080', payload.themeColor)
        assertTrue(shCalls.any { it.contains('-d @teams-payload.json') }, 'should post the payload via -d @file, not inline')
    }

    @Test
    void testCustomMessageContainingQuotesDoesNotBreakPayloadOrShellCommand() {
        def script = loadScript('vars/notifyTeams.groovy')
        script.call([status: 'FAILURE', message: '''deploy to "prod" failed: can't reach host'''])

        def payload = new JsonSlurper().parseText(writtenFiles['teams-payload.json'])
        assertEquals('''deploy to "prod" failed: can't reach host''', payload.sections[0].activityText)
        assertEquals('cc0000', payload.themeColor)
        assertTrue(shCalls.every { !it.contains('reach host') }, 'raw message text must never be interpolated into the shell command')
    }
}
