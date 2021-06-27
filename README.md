[![platform](https://img.shields.io/badge/platform-Android-yellow.svg)](https://www.android.com)
 [![API](https://img.shields.io/badge/API-%2B17-green.svg)]()
 [![](https://jitpack.io/v/Saif-al-islam/NumberSlidingPicker-Android.svg)](https://jitpack.io/#Saif-al-islam/NumberSlidingPicker-Android)


**Number Sliding Picker - Android** - An Android library that can increase and decrease the Count.

## Sample

## Usage

### step 1

Include the library as a local library project or add the dependency in your build.gradle.

```groovy
dependencies {
	   implementation 'com.github.Saif-al-islam:NumberSlidingPicker-Android:1.2.1'
	}
```

Add it in your root build.gradle.

```groovy
allprojects {
		repositories {
			maven { url 'https://jitpack.io' }
		}
	}
```

### Step 2

Include the SliderCounterView in your layout. And you can customize it like this.

```xml
    <com.saif.numberslidingpickerview.NumberSlidingPickerView
        android:id="@+id/num_pick"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:minNum="1"
        app:numMargin="12dp"
        app:unitTopMargin="12dp"
        app:orientation="Horizontal"
        app:unitText="sec"/>
```

## XML Attributes Description

<!-- |name|format|description|
|:---:|:---:|:---:|
| min_counter | integer | Minimum value of the counter, default is 0
| max_counter | integer | Maximum value of the counter and -1 for infinite counter (no maximum number), default is -1
| init_counter | integer | Start value of the counter，default is 0
| step_counter | integer | Increasing value for one step value of the counter，default is 1
| counter_in_milli_seconds | Integer | Speed of the increased or decreased of the counter / the time in milliSeconds between every increased or decreased value , default is 400ms
| start_color_counter | color | Start Color when user start dragging , default is WHITE
| end_color_counter | color | End Color when the counter reach the end of the view , default is WHITE
| radius_counter | dimension | the radius of the view's corner.
 -->

