#ifndef NATIVE_LIBS_SUPERRESOLUTION_H
#define NATIVE_LIBS_SUPERRESOLUTION_H

#include <string>

#include "tensorflow/lite/c/c_api.h"
#include "tensorflow/lite/c/common.h"
#include "tensorflow/lite/delegates/gpu/delegate.h"

#define LOG_TAG "super_resolution::"
#define LOGI(...) \
  ((void)__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__))
#define LOGE(...) \
  ((void)__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__))

namespace yveskalume {
    namespace instaglow {
        namespace superresolution {

            const int kInputImageHeight = 50;
            const int kInputImageWidth = 50;
            const int kImageChannels = 3;
            const int kNumberOfInputPixels = kInputImageHeight * kInputImageWidth;
            const int kUpscaleFactor = 4;
            const int kOutputImageHeight = kInputImageHeight * kUpscaleFactor;
            const int kOutputImageWidth = kInputImageWidth * kUpscaleFactor;
            const int kNumberOfOutputPixels = kOutputImageHeight * kOutputImageWidth;

            class SuperResolution {
            public:
                SuperResolution(const void* model_data, size_t model_size, bool use_gpu);
                ~SuperResolution();
                bool IsInterpreterCreated();
                // DoSuperResolution() performs super resolution on a low resolution image. It
                // returns a valid pointer if successful and nullptr if unsuccessful.
                // lr_img_rgb: the pointer to the RGB array extracted from low resolution
                // image
                std::unique_ptr<int[]> DoSuperResolution(int* lr_img_rgb);

            private:
                // TODO: use unique_ptr
                TfLiteInterpreter* interpreter_;
                TfLiteModel* model_ = nullptr;
                TfLiteInterpreterOptions* options_ = nullptr;
                TfLiteDelegate* delegate_ = nullptr;
            };

        }  // namespace superresolution
    }  // namespace examples
}  // namespace tflite
#endif