package be.remyv.testarcore;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.ux.TransformationSystem;

public class PointUiRenderable extends TransformableNode implements Node.OnTapListener {

    public PointUiRenderable(TransformationSystem transformationSystem) {
        super(transformationSystem);
    }


}
