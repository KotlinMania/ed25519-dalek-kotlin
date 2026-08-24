import Testing
import Ed25519Dalek

@Suite("Ed25519Dalek Swift Export Tests")
struct Ed25519DalekExportTests {
    @Test("Ed25519Dalek constants and Context export correctly")
    func testSwiftModuleLoads() {
        #expect(SIGNATURE_LENGTH == 64)
        #expect(SECRET_KEY_LENGTH == 32)
        #expect(PUBLIC_KEY_LENGTH == 32)
        #expect(KEYPAIR_LENGTH == 64)
        #expect(EXPANDED_SECRET_KEY_LENGTH == 64)
        #expect(Context.Companion.shared.MAX_LENGTH == 255)
    }
}
