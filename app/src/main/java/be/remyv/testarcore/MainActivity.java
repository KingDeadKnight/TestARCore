package be.remyv.testarcore;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import com.google.ar.core.*;
import com.google.ar.sceneform.*;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.*;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity {

    private ArFragment arFragment;
    Renderable redCubeRenderable;
    private List<AnchorNode> anchors = new ArrayList<>();
    Node viewRenderable;
    Node lineNode;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainactivity);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.RED))
                .thenAccept(
                        material -> {
                            redCubeRenderable =
                                    ShapeFactory.makeCube(new Vector3(0.05f, 0.05f, 0.05f), new Vector3(0.0f, 0.0f, 0.0f), material);
                        });
        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (anchors.size() < 1) {
                        AnchorNode anchorNode = new AnchorNode(hitResult.createAnchor());
                        anchorNode.setParent(arFragment.getArSceneView().getScene());
                        anchors.add(anchorNode);
                        TransformableNode Node = new TransformableNode(arFragment.getTransformationSystem());
                        Node.setParent(anchorNode);
                        Node.setRenderable(redCubeRenderable);
                        return;
                    }
                    if (anchors.size() < 2) {
                        AnchorNode anchorNode = new AnchorNode(hitResult.createAnchor());
                        anchorNode.setParent(arFragment.getArSceneView().getScene());
                        anchors.add(anchorNode);
                        TransformableNode Node = new TransformableNode(arFragment.getTransformationSystem());
                        Node.setParent(anchorNode);
                        Node.setRenderable(redCubeRenderable);
                        Pose firstPose = anchorNode.getAnchor().getPose();
                        Pose secondPose = anchors.get(0).getAnchor().getPose();
                        float dx = firstPose.tx() - secondPose.tx();
                        float dy = firstPose.ty() - secondPose.ty();
                        float dz = firstPose.tz() - secondPose.tz();

                        // Compute the straight-line distance.
                        float distanceMeters = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                        Quaternion lookRotation = Quaternion.lookRotation(
                                Vector3.subtract(
                                        anchors.get(0).getWorldPosition(),
                                        anchorNode.getWorldPosition()
                                ),
                                Vector3.up()
                        );
                        this.lineBetweenPoints(anchors.get(0), lookRotation, distanceMeters);
                    } else {
                        AnchorNode anchor = anchors.get(0);
                        anchor.getAnchor().detach();
                        anchor.setParent(null);
                        anchors.remove(anchor);
                        viewRenderable = null;
                        lineNode = null;
                    }
                }
        );

        arFragment.getArSceneView().getScene().addOnUpdateListener( (frameTime) -> {
            arFragment.onUpdate(frameTime);
            onUpdate();
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onUpdate(){
        if(viewRenderable != null){
            Vector3 camPos = arFragment.getArSceneView().getScene().getCamera().getWorldPosition();
            Vector3 uiPos = viewRenderable.getWorldPosition();
            Vector3 dir = Vector3.subtract(camPos, uiPos);
            viewRenderable.setWorldRotation(Quaternion.lookRotation(dir, Vector3.up()));
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    public void lineBetweenPoints(AnchorNode anchorNode, Quaternion lookRotation, float distanceMeters) {
        MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.WHITE))
                .thenAccept(
                        material -> {
                            Log.d("lineBetweenPoints", "distance: " + distanceMeters);
                            Vector3 size = new Vector3(.01f, .01f, distanceMeters);
                            Vector3 center = new Vector3(.01f / 2, .01f / 2, distanceMeters / 2);
                            Renderable line = ShapeFactory.makeCube(size, center, material);
                            lineNode = new Node();
                            final Quaternion rotationFromAToB = lookRotation;
                            lineNode.setParent(anchorNode);
                            lineNode.setRenderable(line);
                            lineNode.setWorldRotation(rotationFromAToB);
                            ViewRenderable.builder()
                                    .setView(arFragment.getContext(), R.layout.test)
                                    .setVerticalAlignment(ViewRenderable.VerticalAlignment.BOTTOM)
                                    .setSizer(new FixedHeightViewSizer(0.04f))
                                    .build()
                                    .thenAccept(
                                          render -> {
                                              render.setShadowCaster(false);
                                              TextView text = render.getView().findViewById(R.id.coucou);
                                              DecimalFormat format = new DecimalFormat("#.##");
                                              text.setText(format.format(distanceMeters) + "m");
                                              viewRenderable = new TransformableNode(arFragment.getTransformationSystem());
                                              viewRenderable.setLocalPosition(new Vector3(0f, 0.1f, 0f));
                                              viewRenderable.setRenderable(render);
                                              viewRenderable.setParent(lineNode);
                                          }
                                    );

                        });
    }
}
