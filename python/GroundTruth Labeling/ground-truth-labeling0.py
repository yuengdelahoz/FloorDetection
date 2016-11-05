#!/usr/bin/env python

from gimpfu import *

def ground_truth_label(img, layer ):
    ''' Save the current layer into a PNG file, a JPEG file and a BMP file.

    Parameters:
    image : image The current image.
    layer : layer The layer of the image that is selected.
    outputFolder : string The folder in which to save the images.
    '''
    # Set up an undo group, so the operation will be undone in one step.
    pdb.gimp_image_undo_group_start(img)

    ''' Invert image selection '''
    pdb.gimp_selection_invert(img)

    ''' Fill background with white color '''
    pdb.gimp_edit_fill(layer, BACKGROUND_FILL)

    outputFolder, trash = img.filename.split('originals')

    try:
        # Save as PNG
        gimp.pdb.file_png_save(img, layer, outputFolder+'/originals2/'+layer.name, "raw_filename", 0, 9, 0, 0, 0, 0, 0)
    except Exception as err:
        gimp.message("Unexpected error: " + str(err))
    pdb.gimp_image_undo_group_end(img)


register(
    "python_fu_generate_ground_truth_0",
    "Generate Ground Truth v2",
    "Generate ground truth v2 and save it in the label folder",
    "YSDI",
    "Nothing v2",
    "October, 2016",
    "<Image>/Filters/Test/Generate Ground Truth v2",
    "*",
    [],
    [],
    ground_truth_label)

main()
