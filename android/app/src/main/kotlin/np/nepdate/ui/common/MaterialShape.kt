@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package np.nepdate.ui.common

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath

/**
 * A [Shape] from one of the Material 3 Expressive [MaterialShapes]. It does what
 * `RoundedPolygon.toShape()` does, without needing composition, so the shapes can be plain values
 * shared by every screen. The polygon's path is built once; each outline only scales it.
 */
internal class MaterialShape(
    polygon: RoundedPolygon,
) : Shape {
    private val unitPath: Path by lazy { polygon.toPath().asComposePath() }

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path = Path()
        path.addPath(unitPath)
        path.transform(Matrix().apply { scale(x = size.width, y = size.height) })
        path.translate(size.center - path.getBounds().center)
        return Outline.Generic(path)
    }
}

/** The Sunny shape: the today marker. Marigold, the Tihar flower and the sun. */
internal val SunnyShape: Shape = MaterialShape(MaterialShapes.Sunny)

/** The nine-sided cookie: the date tile of the day sheet. */
internal val CookieShape: Shape = MaterialShape(MaterialShapes.Cookie9Sided)

/** The four-leaf clover: decoration on the first-run screen. */
internal val CloverShape: Shape = MaterialShape(MaterialShapes.Clover4Leaf)

/** The flower: the quiet marigold drawn behind the today card. */
internal val FlowerShape: Shape = MaterialShape(MaterialShapes.Flower)

/** The diamond: the small event marker under a day. */
internal val DiamondShape: Shape = MaterialShape(MaterialShapes.Diamond)

/** The soft burst: the next-holiday mark on the today card. */
internal val SoftBurstShape: Shape = MaterialShape(MaterialShapes.SoftBurst)
