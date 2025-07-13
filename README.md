# Machine-learning-based-assessment-of-the-tumour-immune-infiltrate
This is the repository for the assessment of tumor immune infiltrate using QuPath.

### **Project preparation**

**Step 1**: Download and install the QuPath on your system from here: https://qupath.github.io/

**Step 2**: Create a QuPath project. You can find the instructions here: https://qupath.readthedocs.io/en/stable/docs/tutorials/projects.html#create-a-project

**Step 3**: Annotate the image areas (https://qupath.readthedocs.io/en/stable/docs/tutorials/cell_detection.html#annotate-a-region-of-interest) or load the annotation files

**Step 4**: Save the project for the further analysis.

-------------------------------------------------------------------------------------------------------------------

## **Color deconvolution** to mitigate stain variabilitis in images

1. Draw a small rectangle in an area that is representative of the stain you want to characterize – or the background.

2. Double-click on the corresponding stain (or background) under the Image tab and follow the prompts to update the vectors.

3. Repeat this for other stains if needed.

4. Use Analyze -> Preprocessing -> Estimate stain vectors. This records stain vectors in the command history.

5. Now, we can generate a script that applies the color deconvolution to all images in the project. To use the script, click on Automate tab -> script editor and use the obtained values like the commands below:

```setImageType('BRIGHTFIELD_H_DAB');```

```setColorDeconvolutionStains('{"Name" : "H&E adjusted", "Stain 1" : "Hematoxylin", "Values 1" : "0.65 0.70 0.29 ", "Stain 2" : "Eiosin", "Values 2" : "0.27 0.56 0.77 ", "Background" : " 255 255 255 "}');```

-------------------------------------------------------------------------------------------------------------------

**StarDist** for cell detection and segmentation

1. Download and install the StarDist extension for QuPath from here: https://github.com/qupath/qupath-extension-stardist

2. To use the StarDist, click on Extensions tab in QuPath -> StarDist -> select the proper model for your task.

##### **Each pretrained model has its own individual .pb file that should be downloaded before using the StartDist.**

##### **QuPath’s StarDist support is only available by scripting.**

3. Select the region you want to perform cell detection and segmentation. It can be one or more boxes, circles, or annotated regions in the image.

4. Run the StarDist script. The following code can be used for H&E-stained images and more examples can be found here: https://qupath.readthedocs.io/en/0.4/docs/deep/stardist.html.

```
import qupath.ext.stardist.StarDist2D

// Specify the model file (you will need to change this!)
def pathModel = '/path/to/he_heavy_augment.pb'

def stardist = StarDist2D.builder(pathModel)
      .threshold(0.5)              // Prediction threshold
      .normalizePercentiles(1, 99) // Percentile normalization
      .pixelSize(0.5)              // Resolution for detection
      .build()

// Run detection for the selected objects
def imageData = getCurrentImageData()
def pathObjects = getSelectedObjects()
if (pathObjects.isEmpty()) {
    Dialogs.showErrorMessage("StarDist", "Please select a parent object!")
    return
}
stardist.detectObjects(imageData, pathObjects)
println 'Done!'
```

5. Measurements can be exported from the Measure tab -> Show annotation/detection measurements for further evaluations.

-------------------------------------------------------------------------------------------------------------------


