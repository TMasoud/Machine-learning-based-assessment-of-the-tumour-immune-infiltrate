import qupath.lib.regions.RegionRequest
import qupath.lib.gui.scripting.QPEx
import qupath.ext.stardist.StarDist2D
server = getCurrentImageData().getServer()

setImageType('BRIGHTFIELD_H_E');
setColorDeconvolutionStains('{"Name" : "H&E default", "Stain 1" : "Hematoxylin", "Values 1" : "0.65111 0.70119 0.29049", "Stain 2" : "Eosin", "Values 2" : "0.2159 0.8012 0.5581", "Background" : " 255 255 255"}');


def imageData = QPEx.getCurrentImageData()
def server = imageData.getServer()
def filename = server.getMetadata().getName()


// Get all annotations labeled "roi"
def roiAnnotations = getAnnotationObjects().findAll {
    it.getPathClass() != null && it.getPathClass().getName() == "roi"
}

// Loop through each "roi" annotation
roiAnnotations.each { anno ->
    // Unlock the annotation for processing if necessary
    anno.setLocked(false)  // Unlock the 'roi' annotation
   
    // Optionally, log the number of objects inside the 'roi'
    def roiObjects = getCurrentHierarchy().getObjectsForROI(qupath.lib.objects.PathDetectionObject, anno.getROI())
    println "Found ${roiObjects.size()} objects inside 'roi' annotation."

    // Lock the annotation and its objects for further processing if necessary
    selectObjectsByClassification("roi");  // Lock the 'roi' annotation after processing
   
    // Lock each object inside the 'roi' annotation
    roiObjects.each { object ->
        object.setLocked(true)  // Lock each object inside the ROI
    }
}

// Optionally, print the number of locked "roi" annotations
def lockedROIs = roiAnnotations.findAll { it.isLocked() }
println "Locked ${lockedROIs.size()} 'roi' annotations."


// Specify the model file (you will need to change this!)
def pathModel = '.../he_heavy_augment.pb'

def stardist = StarDist2D.builder(pathModel)
        .threshold(0.5)              // Prediction threshold
        .normalizePercentiles(1, 99) // Percentile normalization
        .pixelSize(0.5)              // Resolution for detection
        .measureShape()              // Add shape measurements
        .measureIntensity()          // Add cell measurements (in all compartments)
        .includeProbability(true)    // Include prediction probability as measurement
        .cellConstrainScale(1.5)
        .build()

// Run detection for the selected objects
def pathObjects = getSelectedObjects()
if (pathObjects.isEmpty()) {
    Dialogs.showErrorMessage("StarDist", "Please select a parent object!")
    return
}
print filename
stardist.detectObjects(imageData, pathObjects)

selectDetections();
runPlugin('qupath.lib.algorithms.IntensityFeaturesPlugin', '{"pixelSizeMicrons": 0.50,  "region": "ROI",  "tileSizeMicrons": 25.0,  "colorOD": true,  "colorStain1": true,  "colorStain2": true,  "colorStain3": true,  "colorRed": true,  "colorGreen": true,  "colorBlue": true,  "colorHue": true,  "colorSaturation": true,  "colorBrightness": true,  "doMean": true,  "doStdDev": true,  "doMinMax": true,  "doMedian": true,  "doHaralick": true,  "haralickDistance": 1,  "haralickBins": 32}');
selectAnnotations();
runPlugin('qupath.lib.plugins.objects.SmoothFeaturesPlugin', '{"fwhmMicrons": 25.0,  "smoothWithinClasses": false,  "useLegacyNames": false}');
selectAnnotations();
runPlugin('qupath.lib.plugins.objects.SmoothFeaturesPlugin', '{"fwhmMicrons": 50.0,  "smoothWithinClasses": false,  "useLegacyNames": false}');
selectAnnotations();
runPlugin('qupath.lib.plugins.objects.SmoothFeaturesPlugin', '{"fwhmMicrons": 100.0,  "smoothWithinClasses": false,  "useLegacyNames": false}');
selectAnnotations();
runObjectClassifier("path/to/object.classifier.json")

save_path = String.format("/.../Detection/" + filename - ".tif" + ".txt")

saveDetectionMeasurements(save_path)

println 'Done!'


