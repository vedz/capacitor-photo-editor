import Foundation

@objc public class PhotoEditor: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
