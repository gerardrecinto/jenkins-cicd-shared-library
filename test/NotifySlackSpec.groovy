import com.lesfurets.jenkins.unit.BasePipelineTest
import groovy.json.JsonSlurper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.*

class NotifySlackSpec extends BasePipelineTest {

    def shCalls = []
    def writtenFiles = [:]

    @BeforeEach
    void setUp() {
        super.setUp()
        binding.setVariable('env', [BUILD_URL: 'https://ci.example.com/job/1', JOB_NAME: 'my-job', BUILD_NUMBER: '1'])
        helper.registerAllowedMethod('sh', [String.class], { cmd -> shCalls << cmd.trim() })
        helper.registerAllowedMethod('withCredentials', [List.class, Closure.class], { creds, body ->
            binding.setVariable('WEBHOOK_URL', 'https://hooks.slack.test/x')
            body()
        })
        helper.registerAllowedMethod('string', [Map.class], { it })
        helper.registerAllowedMethod('writeFile', [Map.class], { args -> writtenFiles[args.file] = args.text })
    }

    @Test
    void testDefaultConfig() {
        def script = loadScript('vars/notifySlack.groovy')
        script.call([:])

        def payload = new JsonSlurper().parseText(writtenFiles['slack-payload.json'])
        assertEquals('#ci-cd', payload.channel)
        assertEquals('UNKNOWN: my-job #1', payload.attachments[0].text)
        assertTrue(shCalls.any { it.contains('-d @slack-payload.json') }, 'should post the payload via -d @file, not inline')
    }

    @Test
    void testMessageContainingQuotesAndApostrophesDoesNotBreakPayloadOrShellCommand() {
        // The old string-interpolated version either produced invalid JSON
        // or broke out of the shell's single-quoted -d argument entirely
        // when the message contained a ' or ". This is the regression case.
        def script = loadScript('vars/notifySlack.groovy')
        script.call([message: '''can't parse "the config"; it's broken'''])

        def payload = new JsonSlurper().parseText(writtenFiles['slack-payload.json'])
        assertEquals('''can't parse "the config"; it's broken''', payload.attachments[0].text)
        assertTrue(shCalls.every { !it.contains("can't parse") }, 'raw message text must never be interpolated into the shell command')
    }
}
