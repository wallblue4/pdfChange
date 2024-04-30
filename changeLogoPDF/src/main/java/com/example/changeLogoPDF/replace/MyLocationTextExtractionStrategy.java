package com.example.changeLogoPDF.replace;

import com.example.changeLogoPDF.logo.LogoDetector;
import com.example.changeLogoPDF.logo.LogoReplacer;
import com.example.changeLogoPDF.utils.PdfManipulator;
import com.itextpdf.kernel.geom.LineSegment;
import com.itextpdf.kernel.geom.Matrix;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;
import com.itextpdf.kernel.pdf.canvas.parser.listener.TextChunk;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

public class MyLocationTextExtractionStrategy extends LocationTextExtractionStrategy {
    private List<TextChunk> myTextChunks = new ArrayList<>();

//    @Override
//    public void eventOccurred(IEventData data, EventType type) {
//        if (type.equals(EventType.RENDER_TEXT)) {
//            TextRenderInfo renderInfo = (TextRenderInfo) data;
//            for (TextRenderInfo ri : renderInfo.getCharacterRenderInfos()) {
//                LineSegment segment = ri.getBaseline();
//                if (ri.getRise() != 0){
//                    // remove the rise from the baseline - we do this because the text from a
//                    // super/subscript render operations should probably be considered as part
//                    // of the baseline of the text the super/sub is relative to
//                    Matrix riseOffsetTransform = new Matrix(0, -ri.getRise());
//                    segment = segment.transformBy(riseOffsetTransform);
//                }
//                TextChunk location = new TextChunk(ri.getText(), segment.getStartPoint());
//                myTextChunks.add(location);
//            }
//        }
//    }
//
//    public List<TextChunk> getMyTextChunks() {
//        return myTextChunks;
//    }
}

