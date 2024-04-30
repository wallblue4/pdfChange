package com.example.changeLogoPDF;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Vector;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;
import com.itextpdf.kernel.pdf.canvas.parser.listener.TextChunk;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangeLogoPdfApplication {

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

		System.out.print("Ingresa la ruta del archivo PDF: ");
		String pdfPath = scanner.nextLine();
		String[] text = {"Correo electrónico: sales@truora.com","www.truora.com","a",
				"consentimiento de realizar la búsqueda de antecedentes para prevención de fraude, es otorgado a la compañía que está solicitando la verificación de"
		,"antecedentes. Si tiene alguna pregunta o preocupación acerca del consentimiento, por favor contacte a la compañía a la que está aplicando."
		,"Truora no participa en el proceso de contratación y no evalúa la información devuelta en el informe, Truora entrega la información completa a sus clientes los"
		,"cuales la usarán como un parámetro adicional en su toma de decisiones. Cualquier consulta adicional del actual reporte, o aclaracion sobre la información"
		,"Truora obtiene los datos a través de medios remotos o locales de comunicación electrónica, óptica y de otras tecnologías en fuentes de acceso público. El"
		,"encontrada puede comunicarse al correo electrónico contacto@truora.com o visitar el sitio web www.truora.com. Si desea leer nuestra política de privacidad"
				,"completa ingrese en www.truora.com/politicas-privacidad."
		};

		List<String> textToFind = Arrays.asList(text);
		Rectangle rectangle = new Rectangle(50, 50, 200, 200);


		try {
			byte[] pdfBytes = readFileToByteArray(pdfPath);
			byte[] modifiedPdfBytes = removeLogo(pdfBytes, "Im8");
			byte[] modifiedPdfBytes1 = removeLogo(modifiedPdfBytes,"Im57");
			byte[] modifiedPdfBytes2 = drawWhiteRectangle(modifiedPdfBytes1,rectangle);
			findCoordinates(modifiedPdfBytes,"Truora");
			writeByteArrayToFile("pdf_modificado2.pdf", modifiedPdfBytes2);
			findText(pdfBytes,textToFind);


			System.out.println("El PDF con la imagen eliminada se ha guardado en pdf_modificado.pdf");
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	private static byte[] readFileToByteArray(String filePath) throws IOException {
		File file = new File(filePath);
		byte[] bytes = new byte[(int) file.length()];
		try (FileInputStream fis = new FileInputStream(file)) {
			fis.read(bytes);
		}
		return bytes;
	}

	private static void writeByteArrayToFile(String filePath, byte[] bytes) throws IOException {
		File file = new File(filePath);
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(bytes);
		}
	}


	public static void inspectPdfResources(byte[] pdfBytes) throws IOException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes);
		PdfReader reader = new PdfReader(inputStream);
		PdfDocument pdfDoc = new PdfDocument(reader);

		int numberOfPages = pdfDoc.getNumberOfPages();
		System.out.println("Número de páginas: " + numberOfPages);

		for (int i = 1; i <= numberOfPages; i++) {
			PdfPage page = pdfDoc.getPage(i);
			PdfDictionary resources = page.getResources().getResource(PdfName.XObject);

			System.out.println("\nRecursos de la página " + i + ":");

			for (Map.Entry<PdfName, PdfObject> entry : resources.entrySet()) {
				PdfName key = entry.getKey();
				PdfObject value = entry.getValue();

				System.out.println("Clave: " + key.getValue());
				System.out.println("Objeto: " + value.getClass().getName());

				if (value instanceof PdfStream) {
					PdfImageXObject image = new PdfImageXObject((PdfStream) value);
					System.out.println("Imagen - Ancho: " + image.getWidth() + ", Alto: " + image.getHeight());

				}
			}
		}
	}

//	public static byte[] removeLogo(byte[] pdfBytes, String imageKey) throws IOException {
//		ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes);
//		PdfReader reader = new PdfReader(inputStream);
//		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//		PdfWriter writer = new PdfWriter(outputStream);
//		PdfDocument pdfDoc = new PdfDocument(reader, writer);
//
//		int numberOfPages = pdfDoc.getNumberOfPages();
//
//		for (int i = 1; i <= numberOfPages; i++) {
//			PdfPage page = pdfDoc.getPage(i);
//			PdfDictionary resources = page.getResources().getResource(PdfName.XObject);
//
//			for (Map.Entry<PdfName, PdfObject> entry : resources.entrySet()) {
//				PdfName key = entry.getKey();
//				PdfObject value = entry.getValue();
//
//				if (value.isStream()) {
//					PdfStream stream = (PdfStream) value;
//					if (stream.getAsName(PdfName.Subtype).equals(PdfName.Image)) {
//						if (key.getValue().equals(imageKey)) {
//							resources.remove(key); // Remove the image
//						}
//					}
//				} else if (value.isIndirect()) {
//					PdfIndirectReference indirectRef = (PdfIndirectReference) value;
//					PdfObject indirectObj = pdfDoc.getPdfObject(indirectRef.getObjNumber());
//					if (indirectObj.isStream()) {
//						PdfStream stream = (PdfStream) indirectObj;
//						if (stream.getAsName(PdfName.Subtype).equals(PdfName.Image)) {
//							if (key.getValue().equals(imageKey)) {
//								resources.remove(key); // Remove the image
//							}
//						}
//					}
//				}
//			}
//		}
//
//		pdfDoc.close();
//		reader.close();
//
//		return outputStream.toByteArray();
//	}


//	public static byte[] removeImageFromPdf(byte[] pdfBytes, String imageKey) throws IOException {
//		ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes);
//		PdfReader reader = new PdfReader(inputStream);
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		PdfWriter writer = new PdfWriter(baos);
//		PdfDocument pdfDoc = new PdfDocument(reader, writer);
//
//		int numberOfPages = pdfDoc.getNumberOfPages();
//
//		for (int i = 1; i <= numberOfPages; i++) {
//			PdfPage page = pdfDoc.getPage(i);
//			PdfDictionary resources = page.getResources().getResource(PdfName.XObject);
//
//			for (PdfName key : resources.keySet()) {
//				PdfObject value = resources.get(key);
//
//				if (value instanceof PdfStream) {
//					PdfImageXObject image = new PdfImageXObject((PdfStream) value);
//					if (key.getValue().equals(imageKey)) {
//						resources.remove(key); // Eliminar la imagen del diccionario de recursos
//						break; // Salir del bucle una vez que se haya eliminado la imagen
//					}
//				}
//			}
//		}
//
//		pdfDoc.close(); // Cerrar el documento
//		reader.close(); // Cerrar el lector
//
//		// El resultado se encuentra en writer
//
//		byte[] modifiedPdfBytes = baos.toByteArray();		// Aquí puedes guardar el PDF modificado en un archivo o realizar otra acción con él
//		return modifiedPdfBytes;
//	}
public static byte[] removeLogo(byte[] pdfBytes, String imageKey) throws IOException {
	ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes);
	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	PdfReader reader = new PdfReader(inputStream);
	PdfWriter writer = new PdfWriter(outputStream);
	PdfDocument pdfDoc = new PdfDocument(reader, writer);

	int numberOfPages = pdfDoc.getNumberOfPages();

	// Create a transparent image
	BufferedImage transparentImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	Graphics2D g = transparentImage.createGraphics();
	g.setColor(new Color(0, 0, 0, 0));
	g.fillRect(0, 0, 1, 1);
	g.dispose();

	for (int i = 1; i <= numberOfPages; i++) {
		PdfPage page = pdfDoc.getPage(i);
		PdfDictionary resources = page.getResources().getResource(PdfName.XObject);

		for (Map.Entry<PdfName, PdfObject> entry : resources.entrySet()) {
			PdfName key = entry.getKey();
			PdfObject value = entry.getValue();

			if (value.isStream()) {
				PdfStream stream = (PdfStream) value;
				if (stream.getAsName(PdfName.Subtype).equals(PdfName.Image)) {
					if (key.getValue().equals(imageKey)) {
						// Replace the image with a transparent image
						ImageData imageData = ImageDataFactory.create(transparentImage, null);
						PdfImageXObject pdfImageXObject = new PdfImageXObject(imageData);
						resources.put(key, pdfImageXObject.getPdfObject());
					}
				}
			} else if (value.isIndirect()) {
				PdfIndirectReference indirectRef = (PdfIndirectReference) value;
				PdfObject indirectObj = pdfDoc.getPdfObject(indirectRef.getObjNumber());
				if (indirectObj.isStream()) {
					PdfStream stream = (PdfStream) indirectObj;
					if (stream.getAsName(PdfName.Subtype).equals(PdfName.Image)) {
						if (key.getValue().equals(imageKey)) {
							// Replace the image with a transparent image
							ImageData imageData = ImageDataFactory.create(transparentImage, null);
							PdfImageXObject pdfImageXObject = new PdfImageXObject(imageData);
							resources.put(key, pdfImageXObject.getPdfObject());
						}
					}
				}
			}
		}
	}

	pdfDoc.close();
	reader.close();

	return outputStream.toByteArray();
}
	public static byte[] drawWhiteRectangle(byte[] pdfBytes, Rectangle rectangle) throws IOException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PdfReader reader = new PdfReader(inputStream);
		PdfWriter writer = new PdfWriter(outputStream);
		PdfDocument pdfDoc = new PdfDocument(reader, writer);

		int numberOfPages = pdfDoc.getNumberOfPages();

		for (int i = 1; i <= numberOfPages; i++) {
			PdfPage page = pdfDoc.getPage(i);
			PdfCanvas pdfCanvas = new PdfCanvas(page);

			// Set fill color to white
			pdfCanvas.setFillColor(ColorConstants.WHITE);

			// Draw a rectangle
			pdfCanvas.rectangle(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
			pdfCanvas.fill();
		}

		pdfDoc.close();
		reader.close();

		return outputStream.toByteArray();
	}
//	public static void findText(byte[] pdfBytes, String textToFind) throws IOException {
//		ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes);
//		PdfReader reader = new PdfReader(inputStream);
//		PdfDocument pdfDoc = new PdfDocument(reader);
//
//		int numberOfPages = pdfDoc.getNumberOfPages();
//
//		for (int i = 1; i <= numberOfPages; i++) {
//			PdfPage page = pdfDoc.getPage(i);
//			PdfDictionary resources = page.getResources().getResource(PdfName.Font);
//
//			for (Map.Entry<PdfName, PdfObject> entry : resources.entrySet()) {
//				PdfObject value = entry.getValue();
//
//				if (value.isStream()) {
//					PdfStream stream = (PdfStream) value;
//					byte[] bytes = stream.getBytes();
//					String streamContent = new String(bytes);
//					if (streamContent.contains(textToFind)) {
//						System.out.println("El texto se encuentra en la página " + i);
//					}
//				} else if (value.isIndirect()) {
//					PdfIndirectReference indirectRef = (PdfIndirectReference) value;
//					PdfObject indirectObj = pdfDoc.getPdfObject(indirectRef.getObjNumber());
//					if (indirectObj.isStream()) {
//						PdfStream stream = (PdfStream) indirectObj;
//						byte[] bytes = stream.getBytes();
//						String streamContent = new String(bytes);
//						if (streamContent.contains(textToFind)) {
//							System.out.println("El texto se encuentra en la página " + i);
//						}
//					}
//				}
//			}
//		}
//
//		pdfDoc.close();
//	}
public static void findText(byte[] pdfBytes, List<String> textsToFind) throws IOException {
	ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes);
	PdfReader reader = new PdfReader(inputStream);
	PdfDocument pdfDoc = new PdfDocument(reader);

	int numberOfPages = pdfDoc.getNumberOfPages();

	for (int i = 1; i <= numberOfPages; i++) {
		PdfPage page = pdfDoc.getPage(i);
		String pageContent = PdfTextExtractor.getTextFromPage(page);

		for (String textToFind : textsToFind) {
			if (pageContent.contains(textToFind)) {
				System.out.println("El texto '" + textToFind + "' se encuentra en la página " + i);
			}
		}
	}

	pdfDoc.close();
}
	public static void findCoordinates(byte[] pdfBytes, String target) throws IOException {
		PdfDocument pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(pdfBytes)));

		for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
			LocationTextExtractionStrategy strategy = new LocationTextExtractionStrategy() {
				@Override
				public void eventOccurred(IEventData data, EventType type) {
					if (type.equals(EventType.RENDER_TEXT)) {
						TextRenderInfo renderInfo = (TextRenderInfo) data;
						for (TextRenderInfo ri : renderInfo.getCharacterRenderInfos()) {
							String text = ri.getText();
							if (text.contains(target)) {
								com.itextpdf.kernel.geom.Vector startLocation = ri.getBaseline().getStartPoint();
								Vector endLocation = ri.getBaseline().getEndPoint();
								System.out.println("Text: " + text);
								System.out.println("Start Location: " + startLocation);
								System.out.println("End Location: " + endLocation);
							}
						}
					}
				}
			};

			new PdfCanvasProcessor(strategy).processPageContent(pdfDoc.getPage(i));
		}

		pdfDoc.close();
	}
	public static byte[] coverText(byte[] pdfBytes, List<String> textsToCover) throws IOException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PdfReader reader = new PdfReader(inputStream);
		PdfWriter writer = new PdfWriter(outputStream);
		PdfDocument pdfDoc = new PdfDocument(reader, writer);

		int numberOfPages = pdfDoc.getNumberOfPages();

		for (int i = 1; i <= numberOfPages; i++) {
			PdfPage page = pdfDoc.getPage(i);

			PdfTextExtractor.getTextFromPage(page, new LocationTextExtractionStrategy() {
				@Override
				public void eventOccurred(IEventData data, EventType type) {
					if (type.equals(EventType.RENDER_TEXT)) {
						TextRenderInfo renderInfo = (TextRenderInfo) data;
						String text = renderInfo.getText();
						for (String textToCover : textsToCover) {
							String[] wordsToCover = textToCover.split("\\s+");
							for (String wordToCover : wordsToCover) {
								if (text.contains(wordToCover)) {
									com.itextpdf.kernel.geom.Rectangle rect = renderInfo.getBaseline().getBoundingRectangle();
									PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);
									pdfCanvas.saveState()
											.setFillColor(ColorConstants.WHITE)
											.rectangle(rect.getLeft(), rect.getBottom(), rect.getWidth(), rect.getHeight())
											.fill()
											.restoreState();
								}
							}
						}
					}
					super.eventOccurred(data, type);
				}
			});
		}

		pdfDoc.close();

		return outputStream.toByteArray();
	}
	public static byte[] removeContent(byte[] pdfBytes) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(pdfBytes);
		try (PDDocument doc = PDDocument.load(bais)) {
			PDPage page = doc.getPage(4); // get the first page
			PDRectangle mediaBox = page.getMediaBox();

			float lowerLeftX = 0; // replace with your x coordinate
			float lowerLeftY = 0; // replace with your y coordinate
			float upperRightX = mediaBox.getWidth();; // replace with your width
			float upperRightY = mediaBox.getHeight();; // replace with your height

			try (PDPageContentStream contentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.PREPEND, true, true)) {
				contentStream.setNonStrokingColor(Color.WHITE);
				contentStream.addRect(lowerLeftX, mediaBox.getHeight() - upperRightY, upperRightX - lowerLeftX, upperRightY - lowerLeftY);
				contentStream.fill();
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			doc.save(baos);
			return baos.toByteArray(); // return the modified PDF as a byte array
		}
	}
	public static byte[] createPdfWithoutText(byte[] srcBytes, List<String> textToRemove) throws IOException {
		InputStream srcStream = new ByteArrayInputStream(srcBytes);
		ByteArrayOutputStream destStream = new ByteArrayOutputStream();
		PdfReader reader = new PdfReader(srcStream);
		PdfWriter writer = new PdfWriter(destStream);
		PdfDocument pdfDoc = new PdfDocument(reader, writer);


		for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
			PdfPage page = pdfDoc.getPage(i);
			PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);
			com.itextpdf.kernel.geom.Rectangle rectangle = page.getPageSize();
			PdfTextExtractor.getTextFromPage(page, new LocationTextExtractionStrategy() {
				@Override
				public void eventOccurred(IEventData data, EventType type) {
					if (type.equals(EventType.RENDER_TEXT)) {
						TextRenderInfo renderInfo = (TextRenderInfo) data;
						String text = renderInfo.getText();
						for (String str : textToRemove) {

							if (text.contains(str)) {
								com.itextpdf.kernel.geom.Rectangle rect = renderInfo.getBaseline().getBoundingRectangle();
								pdfCanvas.saveState()
										.setFillColor(ColorConstants.WHITE)
										.rectangle(rect.getLeft(), rect.getBottom(), rect.getWidth(), rect.getHeight())
										.fill()
										.restoreState();
							}
						}
					}
					super.eventOccurred(data, type);
				}
			});
		}
		pdfDoc.close();

		return destStream.toByteArray();
	}
	public static byte[] createPdfWithoutText1(byte[] srcBytes, List<String> textsToRemove) throws IOException {
		InputStream srcStream = new ByteArrayInputStream(srcBytes);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PdfReader reader = new PdfReader(srcStream);
		PdfWriter writer = new PdfWriter(outputStream);
		PdfDocument pdfDoc = new PdfDocument(reader, writer);

		int numberOfPages = pdfDoc.getNumberOfPages();

		for (int i = 1; i <= numberOfPages; i++) {
			PdfPage page = pdfDoc.getPage(i);

			PdfTextExtractor.getTextFromPage(page, new LocationTextExtractionStrategy() {
				@Override
				public void eventOccurred(IEventData data, EventType type) {
					if (type.equals(EventType.RENDER_TEXT)) {
						TextRenderInfo renderInfo = (TextRenderInfo) data;
						String text = renderInfo.getText();

						for (String textToRemove : textsToRemove) {
							if (text.contains(textToRemove)) {

								com.itextpdf.kernel.geom.Rectangle rect = renderInfo.getBaseline().getBoundingRectangle();
								PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);
								pdfCanvas.saveState()
										.setFillColor(ColorConstants.WHITE)
										.rectangle(rect.getLeft(), rect.getBottom(), rect.getWidth(), rect.getHeight())
										.fill()
										.restoreState();
							}
						}
					}
					super.eventOccurred(data, type);
				}
			});
		}
		pdfDoc.close();

		return outputStream.toByteArray();
	}
	public static byte[] coverText(byte[] pdfBytes, List<String> textsToCover, boolean replace) throws IOException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PdfReader reader = new PdfReader(inputStream);
		PdfWriter writer = new PdfWriter(outputStream);
		PdfDocument pdfDoc = new PdfDocument(reader, writer);

		int numberOfPages = pdfDoc.getNumberOfPages();

		// Obtén la última página
		PdfPage lastPage = pdfDoc.getPage(numberOfPages);

		// Obtén el diccionario de XObjects
		PdfDictionary xObjectsDictionary = lastPage.getResources().getResource(PdfName.XObject);

		// Obtén el PdfStream con la clave /Im57
		PdfStream contentStream = xObjectsDictionary.getAsStream(new PdfName("Im57"));

		if (contentStream != null) {
			byte[] contentBytes = contentStream.getBytes();
			boolean textFound = removeOrReplaceTextFromPageContent(contentBytes, textsToCover, replace);
			if (textFound) {
				contentStream.setData(contentBytes);
				xObjectsDictionary.put(new PdfName("Im57"), contentStream);
			}
		}

		pdfDoc.close();

		return outputStream.toByteArray();
	}

	private static boolean removeOrReplaceTextFromPageContent(byte[] contentBytes, List<String> textsToRemove, boolean replace) {
		if (contentBytes == null) {
			return false;  // Si contentBytes es null, no hay nada que hacer
		}

		String content = new String(contentBytes);

		StringBuilder modifiedContent = new StringBuilder();
		boolean textFound = false;
		int lastEnd = 0;  // Mover la definición de lastEnd aquí

		for (String textToRemove : textsToRemove) {
			String textPattern = textToRemove.replaceAll("\\n", "\\\\n");
			Pattern pattern = Pattern.compile(textPattern);
			Matcher matcher = pattern.matcher(content);

			while (matcher.find()) {
				textFound = true;
				int start = matcher.start();
				int end = matcher.end();

				modifiedContent.append(content, lastEnd, start);

				if (replace) {
					modifiedContent.append("REEMPLAZADO");
				}

				lastEnd = end;
			}

			content = modifiedContent.toString();
			modifiedContent = new StringBuilder(content);
		}

		if (textFound) {
			modifiedContent.append(content.substring(lastEnd));
			byte[] modifiedBytes = modifiedContent.toString().getBytes();
			return true;
		}

		return false;
	}
}