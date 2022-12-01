import androidx.compose.ui.window.Application
import com.yama.marshal.App
import kotlinx.cinterop.*
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSBundle
import platform.Foundation.NSStringFromClass
import platform.UIKit.*
import platform.UIKit.UIColor.Companion.redColor

fun main() {
    val args = emptyArray<String>()
    memScoped {
        val argc = args.size + 1
        val argv = (arrayOf("skikoApp") + args).map { it.cstr.ptr }.toCValues()
        autoreleasepool {
            UIApplicationMain(argc, argv, null, NSStringFromClass(SkikoAppDelegate))
        }
    }
}

class SkikoAppDelegate : UIResponder, UIApplicationDelegateProtocol {
    companion object : UIResponderMeta(), UIApplicationDelegateProtocolMeta

    @ObjCObjectBase.OverrideInit
    constructor() : super()

    private var _window: UIWindow? = null
    override fun window() = _window
    override fun setWindow(window: UIWindow?) {
        _window = window
    }

    override fun applicationDidFinishLaunching(application: UIApplication) {
        window = UIWindow(UIScreen.mainScreen().bounds())
        window!!.backgroundColor = redColor
        window!!.rootViewController = Application {
            App()
        }
        window!!.makeKeyAndVisible()
    }
}

class AppViewController(nibName: String?, bundle: NSBundle?) : UIViewController(nibName, bundle) {
    private val testView = UIView()

    override fun viewWillAppear(animated: Boolean) {
        super.viewWillAppear(animated)

        testView.setFrame(view.bounds)
        view.addSubview(testView)
    }
}