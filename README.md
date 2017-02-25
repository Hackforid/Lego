#Lego

Lego是一个让你以更优雅的方式来开发RecylerView的Helper。通过他，你能以类似React.js里更组件化更清晰更工程的方式去配置Adpater，同时依靠apt来直接生成高效的diff代码。

在[蛋卷基金](https://danjuanapp.com/)的APP里，我们使用它精简了大量的代码，通过它实现APP的组件化更方便，和Airbnb的epoxy相比，侵入性更小，非常容易迁移，也更容易集成其他工具。



## Example

之前实现一个多ViewType的RecyclerView Adapter需要做很多冗余的事。

```java
public class OldAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ...
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		...
    }

    @Override
    public int getItemCount() {
        ...
    }

    @Override
    public int getItemViewType(int position) {
        ...
    }
}
```

你需要考虑需要多少viewType，需要去根据position计算viewType，需要把各种不同type的view数量加起来作为count... 好累！更重要的是，你需要在`onBindViewHolder`中再根据position、viewType去绑定数据。假如PM有一天让你加几种type或者砍掉某个type，疯了。

业内使用比较多的解决方案是提供一个统一的adapter，但这样侵入性太大，迁移现有代码的成本非常大，使用原生技术在这个“统一”的adapter里实现各种动画效果重构难度也大。

而是用Lego，我们的手法便优雅多了。

我们把每种viewType对应的item封装为一个`component`

```java
@Component
public class SampleComponent extends LegoComponent<ViewHolder, Model> {

    @Override
    protected ViewHolder getViewHolder(ViewGroup container) {
        return new ViewHolder(...);
    }

    @Override
    public void onBindData(ViewHolder viewHolder, Model model) {
        ...
    }
}

public class ViewHolder extends RecyclerView.ViewHolder {
}

public class Model implements LegoModel {
}
```

只用提供ViewHolder和Model，并实现`onBindData`方法，就可以简单的定义一个`component`

然后在Adapter里只需要:

```java
public class NewAdapter extends LegoAdapter {
    public void init() {
        register(new SampleComponent());
    }
}
```

是的，只需要register这个component。That's all !!!



## Installation

```groovy
dependencies {
    compile 'com.github.Hackforid.Lego:annotation:0.1.9'
}

allprojects {
    repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```



## Usage

Lego从React.js和Vue.js里渲染item的方式里得到灵感，通过向Adapter提供Model来直接渲染出列表，开发者只需要提供Model对应的View，而不用去关心其规则细节，从而摆脱对`ViewType`这一概念的冗余实现。

### 1. LegoAdapter

RecyclerView的Adapter需要继承自``LegoAdapter``，并注册```Component```。

```java
public class SampleAdapter extends LegoAdapter {
    public void init() {
        register(new SampleComponent());
    }
}
```



### 2. LegoComponent

``Component``是一个对``ViewHolder``和``Model``的连接，它类似MVP里的presenter，而``ViewHolder``则是View，``Model``便是Model。

```java
@Component
public class SampleComponent extends LegoComponent<ViewHolder, Model> {

    @Override
    protected ViewHolder getViewHolder(ViewGroup container) {
        return new ViewHolder(...);
    }

    @Override
    public void onBindData(ViewHolder viewHolder, Model model) {
        ...
    }
}
```

``Component``需要继承自LegoComponent,并且一定要用Component注解。

你需要实现两个方法：``getViewHolder``和``onBindData``，前者你需要返回这个Component对应的ViewHolder，后者就是普通Adapter里的``onBindViewHolder``，在这里把Model里的数据添加到ViewHolder上。

还有一个``onBindData(V viewHolder, M model, List<Object> payloads)``方法，这个在使用DiffUtil时使用，你可以在后面的高级用法里查询。

### 3. LegoModel

所有提供给Adapter的数据都需要继承自``LegoAdapter``

```java
public static class Model implements LegoModel {
    @LegoIndex
    public String title;

    @LegoField
    public int content;
}
```

然后使用LegoAdapter的``commitData(List<LegoModel> models)``来提交数据。



## DiffUtil

RecyclerView可以使用notifyItemChanged、notifyItemInserted、notifyItemRangeChanged、notifyItemRangeRemoved等方法来高效的更新列表数据，同时，在support lib 24中，Google提供了一个[DiffUtil](https://developer.android.google.cn/reference/android/support/v7/util/DiffUtil.html)来方便我们计算两次数据的变化来自动调用这些方法，具体的使用方法我们在此不介绍。但这个工具的使用依旧不方便，Lego使用annotation processor tool来自动生成DiffUtil需要的代码，我们只需要简单的注解，便能快速使用它。

首先我们需要在调用adapter的``setDiffUtilEnabled``的方法来启动DiffUtil功能，``setDiffUtilDetectMoves``来设置是否使用DiffUtil的detect moves功能。

DiffUtil需要实现两个方法：

1. `` boolean areItemsTheSame(int oldItemPosition, int newItemPosition)``：告诉DiffUtil，两个position是否是同一个Item。

   在LegoModel中使用``@LegoIndex``来标注index，相同的Item应该有相同的Index

   ```java
   @LegoIndex
   public String title;
   ```

   ​

2. ``boolean areContentsTheSame(int oldItemPosition, int newItemPosition)``: 如果是同一个Item，返回这两个Item的数据是否是相等的。

   Lego默认调用mode的``equals``方法来判断Item的数据是否相同，当然如果每一个model都去重载``equals``方法更累，所以Lego提供``@LegoField``注解来方便用户标识需要用来判断的field。

   ```java
   public static class Model implements LegoModel {
       @LegoField public String title;
       @LegoField public int content;
   }
   ```

   这会在内部生成一个类似equals的方法：

   ```java
   // 只是实例 细节会不同 可在LegoFactory里查看
   boolean isModelEqual(oldModel, newModel) {
     	... null check
       return oldModel.title.equals(newModel.title) && oleModel.content.equals(newModel.content);
   }
   ```

   ​

3. `` Object getChangePayload(int oldItemPosition, int newItemPosition) ``: （可选）如果数据不同，返回不同的部分，可以用一个bundle来封装。

   此处涉及ItemView的部分更新，因此需要开发者根据自己的业务自行实现，需要重载Component的``Object getChangePayload(M oldModel, M newModel)``方法，当返回不为null时，载入数据时会先回调Component的``void onBindData(V viewHolder, M model, List<Object> payloads)``方法，用法和DiffUtil及RecyclerView的实现一样，请直接查看相关文档。

### DiffUtil QA

1. 启用DiffUtil后，当数据变化，数据不同的item会闪一下

   这是因为RecyclerView item更新的默认动画有alpha变化的效果，Lego提供一个``NoAlphaDefaultItemAnimator``来方便开发者直接去掉这个alpha动画，就没“白光一闪”的问题了。

   ``recyclerView.setItemAnimator(new NoAlphaDefaultItemAnimator());``



## Other Util

Lego还提供RecyclerView经常会用到的一些工具。

### StickyHeaderRecyclerViewContainer

一个用来实现StickyHeader效果的Layout

```xm
    <com.smilehacker.lego.util.StickyHeaderRecyclerViewContainer
        android:id="@+id/container"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        >
        <android.support.v7.widget.RecyclerView
            android:id="@+id/rv"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            >
        </android.support.v7.widget.RecyclerView>

    </com.smilehacker.lego.util.StickyHeaderRecyclerViewContainer>
```

```java
 mContainer.addHeaderViewType(new HeaderComponent(this).getViewType());
```



License
--------


    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.