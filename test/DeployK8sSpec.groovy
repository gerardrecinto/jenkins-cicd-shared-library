import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.*

class DeployK8sSpec extends BasePipelineTest {

    def shCalls = []
    def echoMsgs = []

    @BeforeEach
    void setUp() {
        super.setUp()
        helper.registerAllowedMethod('sh', [Map.class], { args ->
            shCalls << args.script?.trim()
            if (args.returnStatus) return 0
        })
        helper.registerAllowedMethod('sh', [String.class], { cmd ->
            shCalls << cmd.trim()
        })
        helper.registerAllowedMethod('echo', [String.class], { msg -> echoMsgs << msg })
        helper.registerAllowedMethod('sleep', [Map.class], {})
        helper.registerAllowedMethod('withCredentials', [List.class, Closure.class], { creds, body -> body() })
        helper.registerAllowedMethod('file', [Map.class], { it })
        helper.registerAllowedMethod('error', [String.class], { throw new RuntimeException(it) })
    }

    @Test
    void testDeployWithRolloutCheck() {
        def script = loadScript('vars/deployK8s.groovy')
        script.call([
            manifestPath: 'k8s/production/',
            namespace: 'production',
            kubeConfigCredential: 'kubeconfig-prod',
            deploymentName: 'api',
            timeoutMinutes: 5
        ])

        assertTrue(shCalls.any { it?.contains('kubectl apply') }, 'should apply manifest')
        assertTrue(shCalls.any { it?.contains('rollout status') }, 'should check rollout status')
        assertTrue(shCalls.any { it?.contains('--namespace=production') }, 'should target correct namespace')
    }

    @Test
    void testDeployWithoutDeploymentName() {
        def script = loadScript('vars/deployK8s.groovy')
        script.call([
            manifestPath: 'k8s/',
            namespace: 'staging',
            kubeConfigCredential: 'kubeconfig-staging'
        ])

        assertTrue(shCalls.any { it?.contains('kubectl apply') }, 'should apply manifest')
        assertTrue(shCalls.any { it?.contains('kubectl get pods') }, 'should show pod state when no deployment name given')
    }
}
