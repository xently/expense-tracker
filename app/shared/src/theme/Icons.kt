package co.ke.xently.expense.tracker.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object Icons {
    @Suppress("CheckReturnValue")
    val arrow_forward: ImageVector
        get() {
            if (_arrow_forward != null) {
                return _arrow_forward!!
            }
            _arrow_forward =
                ImageVector.Builder(
                    name = "arrow_forward",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                )
                    .apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1f,
                            stroke = null,
                            strokeAlpha = 1f,
                            strokeLineWidth = 1f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Bevel,
                            strokeLineMiter = 1f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(16.18f, 13f)
                            horizontalLineTo(4f)
                            verticalLineTo(11f)
                            horizontalLineTo(16.18f)
                            lineTo(10.58f, 5.4f)
                            lineTo(12f, 4f)
                            lineToRelative(8f, 8f)
                            lineToRelative(-8f, 8f)
                            lineTo(10.58f, 18.6f)
                            lineTo(16.18f, 13f)
                            close()
                        }
                    }
                    .build()
            return _arrow_forward!!
        }

    private var _arrow_forward: ImageVector? = null

    @Suppress("CheckReturnValue")
    val add: ImageVector
        get() {
            if (_add != null) {
                return _add!!
            }
            _add =
                ImageVector.Builder(
                    name = "add",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                )
                    .apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1f,
                            stroke = null,
                            strokeAlpha = 1f,
                            strokeLineWidth = 1f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Bevel,
                            strokeLineMiter = 1f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(11f, 13f)
                            horizontalLineTo(5f)
                            verticalLineTo(11f)
                            horizontalLineToRelative(6f)
                            verticalLineTo(5f)
                            horizontalLineToRelative(2f)
                            verticalLineToRelative(6f)
                            horizontalLineToRelative(6f)
                            verticalLineToRelative(2f)
                            horizontalLineTo(13f)
                            verticalLineToRelative(6f)
                            horizontalLineTo(11f)
                            verticalLineTo(13f)
                            close()
                        }
                    }
                    .build()
            return _add!!
        }

    private var _add: ImageVector? = null

    @Suppress("CheckReturnValue")
    val toggle_on: ImageVector
        get() {
            if (_toggle_on != null) {
                return _toggle_on!!
            }
            _toggle_on =
                ImageVector.Builder(
                    name = "toggle_on",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                )
                    .apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1f,
                            stroke = null,
                            strokeAlpha = 1f,
                            strokeLineWidth = 1f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Bevel,
                            strokeLineMiter = 1f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(7f, 18f)
                            quadTo(4.5f, 18f, 2.75f, 16.25f)
                            reflectiveQuadTo(1f, 12f)
                            reflectiveQuadTo(2.75f, 7.75f)
                            reflectiveQuadTo(7f, 6f)
                            horizontalLineTo(17f)
                            quadToRelative(2.5f, 0f, 4.25f, 1.75f)
                            reflectiveQuadTo(23f, 12f)
                            reflectiveQuadToRelative(-1.75f, 4.25f)
                            reflectiveQuadTo(17f, 18f)
                            horizontalLineTo(7f)
                            close()
                            moveTo(7f, 16f)
                            horizontalLineTo(17f)
                            quadToRelative(1.65f, 0f, 2.83f, -1.18f)
                            reflectiveQuadTo(21f, 12f)
                            reflectiveQuadTo(19.83f, 9.17f)
                            reflectiveQuadTo(17f, 8f)
                            horizontalLineTo(7f)
                            quadTo(5.35f, 8f, 4.18f, 9.17f)
                            reflectiveQuadTo(3f, 12f)
                            reflectiveQuadToRelative(1.17f, 2.82f)
                            reflectiveQuadTo(7f, 16f)
                            close()
                            moveTo(19.13f, 14.13f)
                            quadTo(20f, 13.25f, 20f, 12f)
                            reflectiveQuadTo(19.13f, 9.88f)
                            reflectiveQuadTo(17f, 9f)
                            reflectiveQuadTo(14.88f, 9.88f)
                            reflectiveQuadTo(14f, 12f)
                            reflectiveQuadToRelative(0.88f, 2.13f)
                            reflectiveQuadTo(17f, 15f)
                            reflectiveQuadToRelative(2.13f, -0.88f)
                            close()
                            moveTo(12f, 12f)
                            close()
                        }
                    }
                    .build()
            return _toggle_on!!
        }

    private var _toggle_on: ImageVector? = null

    @Suppress("CheckReturnValue")
    val toggle_off: ImageVector
        get() {
            if (_toggle_off != null) {
                return _toggle_off!!
            }
            _toggle_off =
                ImageVector.Builder(
                    name = "toggle_off",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                )
                    .apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1f,
                            stroke = null,
                            strokeAlpha = 1f,
                            strokeLineWidth = 1f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Bevel,
                            strokeLineMiter = 1f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(7f, 18f)
                            quadTo(4.5f, 18f, 2.75f, 16.25f)
                            reflectiveQuadTo(1f, 12f)
                            reflectiveQuadTo(2.75f, 7.75f)
                            reflectiveQuadTo(7f, 6f)
                            horizontalLineTo(17f)
                            quadToRelative(2.5f, 0f, 4.25f, 1.75f)
                            reflectiveQuadTo(23f, 12f)
                            reflectiveQuadToRelative(-1.75f, 4.25f)
                            reflectiveQuadTo(17f, 18f)
                            horizontalLineTo(7f)
                            close()
                            moveTo(7f, 16f)
                            horizontalLineTo(17f)
                            quadToRelative(1.65f, 0f, 2.83f, -1.18f)
                            reflectiveQuadTo(21f, 12f)
                            reflectiveQuadTo(19.83f, 9.17f)
                            reflectiveQuadTo(17f, 8f)
                            horizontalLineTo(7f)
                            quadTo(5.35f, 8f, 4.18f, 9.17f)
                            reflectiveQuadTo(3f, 12f)
                            reflectiveQuadToRelative(1.17f, 2.82f)
                            reflectiveQuadTo(7f, 16f)
                            close()
                            moveTo(9.13f, 14.13f)
                            quadTo(10f, 13.25f, 10f, 12f)
                            reflectiveQuadTo(9.13f, 9.88f)
                            reflectiveQuadTo(7f, 9f)
                            reflectiveQuadTo(4.88f, 9.88f)
                            reflectiveQuadTo(4f, 12f)
                            reflectiveQuadToRelative(0.88f, 2.13f)
                            reflectiveQuadTo(7f, 15f)
                            reflectiveQuadTo(9.13f, 14.13f)
                            close()
                            moveTo(12f, 12f)
                            close()
                        }
                    }
                    .build()
            return _toggle_off!!
        }

    private var _toggle_off: ImageVector? = null

    @Suppress("CheckReturnValue")
    val menu: ImageVector
        get() {
            if (_menu != null) {
                return _menu!!
            }
            _menu =
                ImageVector.Builder(
                    name = "menu",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                )
                    .apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1f,
                            stroke = null,
                            strokeAlpha = 1f,
                            strokeLineWidth = 1f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Bevel,
                            strokeLineMiter = 1f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(3f, 18f)
                            verticalLineTo(16f)
                            horizontalLineTo(21f)
                            verticalLineToRelative(2f)
                            horizontalLineTo(3f)
                            close()
                            moveTo(3f, 13f)
                            verticalLineTo(11f)
                            horizontalLineTo(21f)
                            verticalLineToRelative(2f)
                            horizontalLineTo(3f)
                            close()
                            moveTo(3f, 8f)
                            verticalLineTo(6f)
                            horizontalLineTo(21f)
                            verticalLineTo(8f)
                            horizontalLineTo(3f)
                            close()
                        }
                    }
                    .build()
            return _menu!!
        }

    private var _menu: ImageVector? = null

    @Suppress("CheckReturnValue")
    val logout: ImageVector
        get() {
            if (_logout != null) {
                return _logout!!
            }
            _logout =
                ImageVector.Builder(
                    name = "logout",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                )
                    .apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1f,
                            stroke = null,
                            strokeAlpha = 1f,
                            strokeLineWidth = 1f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Bevel,
                            strokeLineMiter = 1f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(5f, 21f)
                            quadTo(4.18f, 21f, 3.59f, 20.41f)
                            reflectiveQuadTo(3f, 19f)
                            verticalLineTo(5f)
                            quadTo(3f, 4.17f, 3.59f, 3.59f)
                            reflectiveQuadTo(5f, 3f)
                            horizontalLineToRelative(7f)
                            verticalLineTo(5f)
                            horizontalLineTo(5f)
                            verticalLineTo(19f)
                            horizontalLineToRelative(7f)
                            verticalLineToRelative(2f)
                            horizontalLineTo(5f)
                            close()
                            moveTo(16f, 17f)
                            lineTo(14.63f, 15.55f)
                            lineTo(17.18f, 13f)
                            horizontalLineTo(9f)
                            verticalLineTo(11f)
                            horizontalLineToRelative(8.18f)
                            lineTo(14.63f, 8.45f)
                            lineTo(16f, 7f)
                            lineToRelative(5f, 5f)
                            lineToRelative(-5f, 5f)
                            close()
                        }
                    }
                    .build()
            return _logout!!
        }

    private var _logout: ImageVector? = null

    @Suppress("CheckReturnValue")
    val sync_alt: ImageVector
        get() {
            if (_sync_alt != null) {
                return _sync_alt!!
            }
            _sync_alt =
                ImageVector.Builder(
                    name = "sync_alt",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                )
                    .apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1f,
                            stroke = null,
                            strokeAlpha = 1f,
                            strokeLineWidth = 1f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Bevel,
                            strokeLineMiter = 1f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(7f, 21f)
                            lineTo(2f, 16f)
                            lineTo(7f, 11f)
                            lineToRelative(1.43f, 1.4f)
                            lineTo(5.83f, 15f)
                            horizontalLineTo(21f)
                            verticalLineToRelative(2f)
                            horizontalLineTo(5.83f)
                            lineToRelative(2.6f, 2.6f)
                            lineTo(7f, 21f)
                            close()
                            moveTo(17f, 13f)
                            lineTo(15.58f, 11.6f)
                            lineTo(18.18f, 9f)
                            horizontalLineTo(3f)
                            verticalLineTo(7f)
                            horizontalLineTo(18.18f)
                            lineTo(15.58f, 4.4f)
                            lineTo(17f, 3f)
                            lineToRelative(5f, 5f)
                            lineToRelative(-5f, 5f)
                            close()
                        }
                    }
                    .build()
            return _sync_alt!!
        }

    private var _sync_alt: ImageVector? = null

    @Suppress("CheckReturnValue")
    val more_vert: ImageVector
        get() {
            if (_more_vert != null) {
                return _more_vert!!
            }
            _more_vert =
                ImageVector.Builder(
                    name = "more_vert",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                )
                    .apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1f,
                            stroke = null,
                            strokeAlpha = 1f,
                            strokeLineWidth = 1f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Bevel,
                            strokeLineMiter = 1f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(12f, 20f)
                            quadToRelative(-0.82f, 0f, -1.41f, -0.59f)
                            reflectiveQuadTo(10f, 18f)
                            reflectiveQuadToRelative(0.59f, -1.41f)
                            reflectiveQuadTo(12f, 16f)
                            reflectiveQuadToRelative(1.41f, 0.59f)
                            quadTo(14f, 17.18f, 14f, 18f)
                            reflectiveQuadToRelative(-0.59f, 1.41f)
                            reflectiveQuadTo(12f, 20f)
                            close()
                            moveToRelative(0f, -6f)
                            quadToRelative(-0.82f, 0f, -1.41f, -0.59f)
                            reflectiveQuadTo(10f, 12f)
                            reflectiveQuadToRelative(0.59f, -1.41f)
                            reflectiveQuadTo(12f, 10f)
                            reflectiveQuadToRelative(1.41f, 0.59f)
                            quadTo(14f, 11.18f, 14f, 12f)
                            reflectiveQuadToRelative(-0.59f, 1.41f)
                            reflectiveQuadTo(12f, 14f)
                            close()
                            moveTo(12f, 8f)
                            quadTo(11.18f, 8f, 10.59f, 7.41f)
                            reflectiveQuadTo(10f, 6f)
                            reflectiveQuadTo(10.59f, 4.59f)
                            reflectiveQuadTo(12f, 4f)
                            reflectiveQuadToRelative(1.41f, 0.59f)
                            quadTo(14f, 5.18f, 14f, 6f)
                            reflectiveQuadTo(13.41f, 7.41f)
                            reflectiveQuadTo(12f, 8f)
                            close()
                        }
                    }
                    .build()
            return _more_vert!!
        }

    private var _more_vert: ImageVector? = null

    @Suppress("CheckReturnValue")
    val dashboard: ImageVector
        get() {
            if (_dashboard != null) {
                return _dashboard!!
            }
            _dashboard =
                ImageVector.Builder(
                    name = "dashboard",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                )
                    .apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1f,
                            stroke = null,
                            strokeAlpha = 1f,
                            strokeLineWidth = 1f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Bevel,
                            strokeLineMiter = 1f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(13f, 9f)
                            verticalLineTo(3f)
                            horizontalLineToRelative(8f)
                            verticalLineTo(9f)
                            horizontalLineTo(13f)
                            close()
                            moveTo(3f, 13f)
                            verticalLineTo(3f)
                            horizontalLineToRelative(8f)
                            verticalLineTo(13f)
                            horizontalLineTo(3f)
                            close()
                            moveToRelative(10f, 8f)
                            verticalLineTo(11f)
                            horizontalLineToRelative(8f)
                            verticalLineTo(21f)
                            horizontalLineTo(13f)
                            close()
                            moveTo(3f, 21f)
                            verticalLineTo(15f)
                            horizontalLineToRelative(8f)
                            verticalLineToRelative(6f)
                            horizontalLineTo(3f)
                            close()
                            moveTo(5f, 11f)
                            horizontalLineTo(9f)
                            verticalLineTo(5f)
                            horizontalLineTo(5f)
                            verticalLineToRelative(6f)
                            close()
                            moveToRelative(10f, 8f)
                            horizontalLineToRelative(4f)
                            verticalLineTo(13f)
                            horizontalLineTo(15f)
                            verticalLineToRelative(6f)
                            close()
                            moveTo(15f, 7f)
                            horizontalLineToRelative(4f)
                            verticalLineTo(5f)
                            horizontalLineTo(15f)
                            verticalLineTo(7f)
                            close()
                            moveTo(5f, 19f)
                            horizontalLineTo(9f)
                            verticalLineTo(17f)
                            horizontalLineTo(5f)
                            verticalLineToRelative(2f)
                            close()
                            moveTo(9f, 11f)
                            close()
                            moveTo(15f, 7f)
                            close()
                            moveToRelative(0f, 6f)
                            close()
                            moveTo(9f, 17f)
                            close()
                        }
                    }
                    .build()
            return _dashboard!!
        }

    private var _dashboard: ImageVector? = null
    @Suppress("CheckReturnValue")
    public val delete: ImageVector
        get() {
            if (_delete != null) {
                return _delete!!
            }
            _delete =
                ImageVector.Builder(
                    name = "delete",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                )
                    .apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1f,
                            stroke = null,
                            strokeAlpha = 1f,
                            strokeLineWidth = 1f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Bevel,
                            strokeLineMiter = 1f,
                            pathFillType = PathFillType.Companion.NonZero,
                        ) {
                            moveTo(7f, 21f)
                            quadTo(6.18f, 21f, 5.59f, 20.41f)
                            reflectiveQuadTo(5f, 19f)
                            verticalLineTo(6f)
                            horizontalLineTo(4f)
                            verticalLineTo(4f)
                            horizontalLineTo(9f)
                            verticalLineTo(3f)
                            horizontalLineToRelative(6f)
                            verticalLineTo(4f)
                            horizontalLineToRelative(5f)
                            verticalLineTo(6f)
                            horizontalLineTo(19f)
                            verticalLineTo(19f)
                            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                            reflectiveQuadTo(17f, 21f)
                            horizontalLineTo(7f)
                            close()
                            moveTo(17f, 6f)
                            horizontalLineTo(7f)
                            verticalLineTo(19f)
                            horizontalLineTo(17f)
                            verticalLineTo(6f)
                            close()
                            moveTo(9f, 17f)
                            horizontalLineToRelative(2f)
                            verticalLineTo(8f)
                            horizontalLineTo(9f)
                            verticalLineToRelative(9f)
                            close()
                            moveToRelative(4f, 0f)
                            horizontalLineToRelative(2f)
                            verticalLineTo(8f)
                            horizontalLineTo(13f)
                            verticalLineToRelative(9f)
                            close()
                            moveTo(7f, 6f)
                            verticalLineTo(19f)
                            verticalLineTo(6f)
                            close()
                        }
                    }
                    .build()
            return _delete!!
        }

    private var _delete: ImageVector? = null
    @Suppress("CheckReturnValue")
    val expenses: ImageVector
        get() {
            if (_expenses != null) {
                return _expenses!!
            }
            _expenses =
                ImageVector.Builder(
                    name = "expenses",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                )
                    .apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1f,
                            stroke = null,
                            strokeAlpha = 1f,
                            strokeLineWidth = 1f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Bevel,
                            strokeLineMiter = 1f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(14f, 13f)
                            quadToRelative(-1.25f, 0f, -2.13f, -0.88f)
                            reflectiveQuadTo(11f, 10f)
                            reflectiveQuadTo(11.88f, 7.88f)
                            reflectiveQuadTo(14f, 7f)
                            reflectiveQuadToRelative(2.13f, 0.88f)
                            reflectiveQuadTo(17f, 10f)
                            reflectiveQuadToRelative(-0.88f, 2.13f)
                            reflectiveQuadTo(14f, 13f)
                            close()
                            moveTo(7f, 16f)
                            quadTo(6.18f, 16f, 5.59f, 15.41f)
                            reflectiveQuadTo(5f, 14f)
                            verticalLineTo(6f)
                            quadTo(5f, 5.18f, 5.59f, 4.59f)
                            reflectiveQuadTo(7f, 4f)
                            horizontalLineTo(21f)
                            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                            quadTo(23f, 5.18f, 23f, 6f)
                            verticalLineToRelative(8f)
                            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                            reflectiveQuadTo(21f, 16f)
                            horizontalLineTo(7f)
                            close()
                            moveTo(9f, 14f)
                            horizontalLineTo(19f)
                            quadToRelative(0f, -0.83f, 0.59f, -1.41f)
                            reflectiveQuadTo(21f, 12f)
                            verticalLineTo(8f)
                            quadTo(20.18f, 8f, 19.59f, 7.41f)
                            reflectiveQuadTo(19f, 6f)
                            horizontalLineTo(9f)
                            quadTo(9f, 6.82f, 8.41f, 7.41f)
                            quadTo(7.83f, 8f, 7f, 8f)
                            verticalLineToRelative(4f)
                            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                            quadTo(9f, 13.18f, 9f, 14f)
                            close()
                            moveToRelative(11f, 6f)
                            horizontalLineTo(3f)
                            quadTo(2.18f, 20f, 1.59f, 19.41f)
                            reflectiveQuadTo(1f, 18f)
                            verticalLineTo(7f)
                            horizontalLineTo(3f)
                            verticalLineTo(18f)
                            horizontalLineTo(20f)
                            verticalLineToRelative(2f)
                            close()
                            moveTo(7f, 14f)
                            verticalLineTo(6f)
                            verticalLineToRelative(8f)
                            close()
                        }
                    }
                    .build()
            return _expenses!!
        }

    private var _expenses: ImageVector? = null

    @Suppress("CheckReturnValue")
    val partners: ImageVector
        get() {
            if (_partners != null) {
                return _partners!!
            }
            _partners =
                ImageVector.Builder(
                    name = "partners",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                )
                    .apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1f,
                            stroke = null,
                            strokeAlpha = 1f,
                            strokeLineWidth = 1f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Bevel,
                            strokeLineMiter = 1f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(1f, 20f)
                            verticalLineTo(16f)
                            quadTo(1f, 15.15f, 1.59f, 14.58f)
                            reflectiveQuadTo(3f, 14f)
                            horizontalLineTo(6.28f)
                            quadToRelative(0.5f, 0f, 0.95f, 0.25f)
                            reflectiveQuadToRelative(0.72f, 0.67f)
                            quadToRelative(0.73f, 0.98f, 1.79f, 1.53f)
                            reflectiveQuadTo(12f, 17f)
                            quadToRelative(1.23f, 0f, 2.29f, -0.55f)
                            reflectiveQuadToRelative(1.76f, -1.53f)
                            quadToRelative(0.32f, -0.42f, 0.76f, -0.67f)
                            reflectiveQuadTo(17.73f, 14f)
                            horizontalLineTo(21f)
                            quadToRelative(0.85f, 0f, 1.43f, 0.57f)
                            reflectiveQuadTo(23f, 16f)
                            verticalLineToRelative(4f)
                            horizontalLineTo(16f)
                            verticalLineTo(17.73f)
                            quadToRelative(-0.88f, 0.63f, -1.89f, 0.95f)
                            reflectiveQuadTo(12f, 19f)
                            quadTo(10.93f, 19f, 9.9f, 18.66f)
                            reflectiveQuadTo(8f, 17.7f)
                            verticalLineTo(20f)
                            horizontalLineTo(1f)
                            close()
                            moveTo(12f, 16f)
                            quadToRelative(-0.95f, 0f, -1.8f, -0.44f)
                            reflectiveQuadTo(8.78f, 14.35f)
                            quadTo(8.35f, 13.73f, 7.71f, 13.36f)
                            reflectiveQuadTo(6.33f, 13f)
                            quadTo(6.88f, 12.08f, 8.65f, 11.54f)
                            reflectiveQuadTo(12f, 11f)
                            reflectiveQuadToRelative(3.35f, 0.54f)
                            reflectiveQuadTo(17.68f, 13f)
                            quadToRelative(-0.73f, 0f, -1.38f, 0.36f)
                            quadToRelative(-0.65f, 0.36f, -1.07f, 0.99f)
                            quadToRelative(-0.55f, 0.8f, -1.4f, 1.22f)
                            reflectiveQuadTo(12f, 16f)
                            close()
                            moveTo(4f, 13f)
                            quadTo(2.75f, 13f, 1.88f, 12.13f)
                            reflectiveQuadTo(1f, 10f)
                            quadTo(1f, 8.73f, 1.88f, 7.86f)
                            reflectiveQuadTo(4f, 7f)
                            quadTo(5.28f, 7f, 6.14f, 7.86f)
                            reflectiveQuadTo(7f, 10f)
                            quadToRelative(0f, 1.25f, -0.86f, 2.13f)
                            reflectiveQuadTo(4f, 13f)
                            close()
                            moveToRelative(16f, 0f)
                            quadToRelative(-1.25f, 0f, -2.13f, -0.88f)
                            reflectiveQuadTo(17f, 10f)
                            quadTo(17f, 8.73f, 17.88f, 7.86f)
                            reflectiveQuadTo(20f, 7f)
                            quadToRelative(1.28f, 0f, 2.14f, 0.86f)
                            reflectiveQuadTo(23f, 10f)
                            quadToRelative(0f, 1.25f, -0.86f, 2.13f)
                            reflectiveQuadTo(20f, 13f)
                            close()
                            moveTo(12f, 10f)
                            quadTo(10.75f, 10f, 9.88f, 9.13f)
                            reflectiveQuadTo(9f, 7f)
                            quadTo(9f, 5.72f, 9.88f, 4.86f)
                            reflectiveQuadTo(12f, 4f)
                            quadToRelative(1.28f, 0f, 2.14f, 0.86f)
                            quadTo(15f, 5.72f, 15f, 7f)
                            quadToRelative(0f, 1.25f, -0.86f, 2.13f)
                            reflectiveQuadTo(12f, 10f)
                            close()
                        }
                    }
                    .build()
            return _partners!!
        }

    private var _partners: ImageVector? = null

    @Suppress("CheckReturnValue")
    val calendar_month: ImageVector
        get() {
            if (_calendar_month != null) {
                return _calendar_month!!
            }
            _calendar_month =
                ImageVector.Builder(
                    name = "calendar_month",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                ).apply {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1f,
                        stroke = null,
                        strokeAlpha = 1f,
                        strokeLineWidth = 1f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Bevel,
                        strokeLineMiter = 1f,
                        pathFillType = PathFillType.NonZero,
                    ) {
                        moveTo(5f, 22f)
                        quadTo(4.18f, 22f, 3.59f, 21.41f)
                        reflectiveQuadTo(3f, 20f)
                        verticalLineTo(6f)
                        quadTo(3f, 5.18f, 3.59f, 4.59f)
                        reflectiveQuadTo(5f, 4f)
                        horizontalLineTo(6f)
                        verticalLineTo(2f)
                        horizontalLineTo(8f)
                        verticalLineTo(4f)
                        horizontalLineToRelative(8f)
                        verticalLineTo(2f)
                        horizontalLineToRelative(2f)
                        verticalLineTo(4f)
                        horizontalLineToRelative(1f)
                        quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                        quadTo(21f, 5.18f, 21f, 6f)
                        verticalLineTo(20f)
                        quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                        reflectiveQuadTo(19f, 22f)
                        horizontalLineTo(5f)
                        close()
                        moveTo(5f, 20f)
                        horizontalLineTo(19f)
                        verticalLineTo(10f)
                        horizontalLineTo(5f)
                        verticalLineTo(20f)
                        close()
                        moveTo(5f, 8f)
                        horizontalLineTo(19f)
                        verticalLineTo(6f)
                        horizontalLineTo(5f)
                        verticalLineTo(8f)
                        close()
                        moveTo(5f, 8f)
                        verticalLineTo(6f)
                        verticalLineTo(8f)
                        close()
                        moveToRelative(7f, 6f)
                        quadToRelative(-0.42f, 0f, -0.71f, -0.29f)
                        quadTo(11f, 13.43f, 11f, 13f)
                        reflectiveQuadToRelative(0.29f, -0.71f)
                        reflectiveQuadTo(12f, 12f)
                        reflectiveQuadToRelative(0.71f, 0.29f)
                        reflectiveQuadTo(13f, 13f)
                        reflectiveQuadToRelative(-0.29f, 0.71f)
                        reflectiveQuadTo(12f, 14f)
                        close()
                        moveTo(7.29f, 13.71f)
                        quadTo(7f, 13.43f, 7f, 13f)
                        reflectiveQuadTo(7.29f, 12.29f)
                        reflectiveQuadTo(8f, 12f)
                        reflectiveQuadToRelative(0.71f, 0.29f)
                        reflectiveQuadTo(9f, 13f)
                        reflectiveQuadTo(8.71f, 13.71f)
                        reflectiveQuadTo(8f, 14f)
                        quadTo(7.58f, 14f, 7.29f, 13.71f)
                        close()
                        moveTo(16f, 14f)
                        quadToRelative(-0.42f, 0f, -0.71f, -0.29f)
                        quadTo(15f, 13.43f, 15f, 13f)
                        reflectiveQuadToRelative(0.29f, -0.71f)
                        reflectiveQuadTo(16f, 12f)
                        quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                        reflectiveQuadTo(17f, 13f)
                        reflectiveQuadToRelative(-0.29f, 0.71f)
                        reflectiveQuadTo(16f, 14f)
                        close()
                        moveToRelative(-4f, 4f)
                        quadToRelative(-0.42f, 0f, -0.71f, -0.29f)
                        quadTo(11f, 17.43f, 11f, 17f)
                        reflectiveQuadToRelative(0.29f, -0.71f)
                        reflectiveQuadTo(12f, 16f)
                        reflectiveQuadToRelative(0.71f, 0.29f)
                        reflectiveQuadTo(13f, 17f)
                        reflectiveQuadToRelative(-0.29f, 0.71f)
                        reflectiveQuadTo(12f, 18f)
                        close()
                        moveTo(7.29f, 17.71f)
                        quadTo(7f, 17.43f, 7f, 17f)
                        reflectiveQuadTo(7.29f, 16.29f)
                        reflectiveQuadTo(8f, 16f)
                        reflectiveQuadToRelative(0.71f, 0.29f)
                        reflectiveQuadTo(9f, 17f)
                        reflectiveQuadTo(8.71f, 17.71f)
                        reflectiveQuadTo(8f, 18f)
                        quadTo(7.58f, 18f, 7.29f, 17.71f)
                        close()
                        moveTo(16f, 18f)
                        quadToRelative(-0.42f, 0f, -0.71f, -0.29f)
                        quadTo(15f, 17.43f, 15f, 17f)
                        reflectiveQuadToRelative(0.29f, -0.71f)
                        reflectiveQuadTo(16f, 16f)
                        quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                        reflectiveQuadTo(17f, 17f)
                        reflectiveQuadToRelative(-0.29f, 0.71f)
                        reflectiveQuadTo(16f, 18f)
                        close()
                    }
                }
                    .build()
            return _calendar_month!!
        }

    @Suppress("CheckReturnValue")
    val notifications_active: ImageVector
        get() {
            if (_notifications_active != null) {
                return _notifications_active!!
            }
            _notifications_active =
                ImageVector.Builder(
                    name = "notifications_active",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                )
                    .apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1f,
                            stroke = null,
                            strokeAlpha = 1f,
                            strokeLineWidth = 1f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Bevel,
                            strokeLineMiter = 1f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(2f, 10f)
                            quadTo(2f, 7.5f, 3.11f, 5.41f)
                            reflectiveQuadTo(6.1f, 1.95f)
                            lineToRelative(1.17f, 1.6f)
                            quadTo(5.78f, 4.65f, 4.89f, 6.32f)
                            reflectiveQuadTo(4f, 10f)
                            horizontalLineTo(2f)
                            close()
                            moveToRelative(18f, 0f)
                            quadTo(20f, 8f, 19.11f, 6.32f)
                            reflectiveQuadTo(16.73f, 3.55f)
                            lineTo(17.9f, 1.95f)
                            quadToRelative(1.88f, 1.38f, 2.99f, 3.46f)
                            reflectiveQuadTo(22f, 10f)
                            horizontalLineTo(20f)
                            close()
                            moveTo(4f, 19f)
                            verticalLineTo(17f)
                            horizontalLineTo(6f)
                            verticalLineTo(10f)
                            quadTo(6f, 7.93f, 7.25f, 6.31f)
                            reflectiveQuadTo(10.5f, 4.2f)
                            verticalLineTo(3.5f)
                            quadToRelative(0f, -0.63f, 0.44f, -1.06f)
                            reflectiveQuadTo(12f, 2f)
                            reflectiveQuadToRelative(1.06f, 0.44f)
                            reflectiveQuadTo(13.5f, 3.5f)
                            verticalLineTo(4.2f)
                            quadToRelative(2f, 0.5f, 3.25f, 2.11f)
                            reflectiveQuadTo(18f, 10f)
                            verticalLineToRelative(7f)
                            horizontalLineToRelative(2f)
                            verticalLineToRelative(2f)
                            horizontalLineTo(4f)
                            close()
                            moveToRelative(8f, -7.5f)
                            close()
                            moveTo(12f, 22f)
                            quadToRelative(-0.82f, 0f, -1.41f, -0.59f)
                            reflectiveQuadTo(10f, 20f)
                            horizontalLineToRelative(4f)
                            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                            reflectiveQuadTo(12f, 22f)
                            close()
                            moveTo(8f, 17f)
                            horizontalLineToRelative(8f)
                            verticalLineTo(10f)
                            quadTo(16f, 8.35f, 14.83f, 7.18f)
                            reflectiveQuadTo(12f, 6f)
                            reflectiveQuadTo(9.18f, 7.18f)
                            reflectiveQuadTo(8f, 10f)
                            verticalLineToRelative(7f)
                            close()
                        }
                    }
                    .build()
            return _notifications_active!!
        }

    private var _notifications_active: ImageVector? = null

    @Suppress("CheckReturnValue")
    val mail: ImageVector
        get() {
            if (_mail != null) {
                return _mail!!
            }
            _mail =
                ImageVector.Builder(
                    name = "mail",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                )
                    .apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1f,
                            stroke = null,
                            strokeAlpha = 1f,
                            strokeLineWidth = 1f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Bevel,
                            strokeLineMiter = 1f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(4f, 20f)
                            quadTo(3.18f, 20f, 2.59f, 19.41f)
                            reflectiveQuadTo(2f, 18f)
                            verticalLineTo(6f)
                            quadTo(2f, 5.18f, 2.59f, 4.59f)
                            reflectiveQuadTo(4f, 4f)
                            horizontalLineTo(20f)
                            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                            quadTo(22f, 5.18f, 22f, 6f)
                            verticalLineTo(18f)
                            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                            reflectiveQuadTo(20f, 20f)
                            horizontalLineTo(4f)
                            close()
                            moveToRelative(8f, -7f)
                            lineTo(4f, 8f)
                            verticalLineTo(18f)
                            horizontalLineTo(20f)
                            verticalLineTo(8f)
                            lineToRelative(-8f, 5f)
                            close()
                            moveToRelative(0f, -2f)
                            lineTo(20f, 6f)
                            horizontalLineTo(4f)
                            lineToRelative(8f, 5f)
                            close()
                            moveTo(4f, 8f)
                            verticalLineTo(6f)
                            verticalLineTo(8f)
                            verticalLineTo(18f)
                            verticalLineTo(8f)
                            close()
                        }
                    }
                    .build()
            return _mail!!
        }

    private var _mail: ImageVector? = null

    @Suppress("CheckReturnValue")
    val revenue_split: ImageVector
        get() {
            if (_revenue_split != null) {
                return _revenue_split!!
            }
            _revenue_split =
                ImageVector.Builder(
                    name = "revenue_split",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                )
                    .apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1f,
                            stroke = null,
                            strokeAlpha = 1f,
                            strokeLineWidth = 1f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Bevel,
                            strokeLineMiter = 1f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(2f, 21f)
                            verticalLineTo(19f)
                            horizontalLineToRelative(9f)
                            verticalLineTo(7.82f)
                            quadTo(10.35f, 7.6f, 9.88f, 7.13f)
                            reflectiveQuadTo(9.18f, 6f)
                            horizontalLineTo(6f)
                            lineToRelative(3f, 7f)
                            quadToRelative(0f, 1.25f, -1.02f, 2.13f)
                            reflectiveQuadTo(5.5f, 16f)
                            reflectiveQuadTo(3.03f, 15.13f)
                            reflectiveQuadTo(2f, 13f)
                            lineTo(5f, 6f)
                            horizontalLineTo(3f)
                            verticalLineTo(4f)
                            horizontalLineTo(9.18f)
                            quadTo(9.48f, 3.13f, 10.25f, 2.56f)
                            reflectiveQuadTo(12f, 2f)
                            reflectiveQuadToRelative(1.75f, 0.56f)
                            quadTo(14.53f, 3.13f, 14.83f, 4f)
                            horizontalLineTo(21f)
                            verticalLineTo(6f)
                            horizontalLineTo(19f)
                            lineToRelative(3f, 7f)
                            quadToRelative(0f, 1.25f, -1.02f, 2.13f)
                            reflectiveQuadTo(18.5f, 16f)
                            reflectiveQuadTo(16.03f, 15.13f)
                            reflectiveQuadTo(15f, 13f)
                            lineTo(18f, 6f)
                            horizontalLineTo(14.83f)
                            quadTo(14.6f, 6.65f, 14.13f, 7.13f)
                            reflectiveQuadTo(13f, 7.82f)
                            verticalLineTo(19f)
                            horizontalLineToRelative(9f)
                            verticalLineToRelative(2f)
                            horizontalLineTo(2f)
                            close()
                            moveTo(16.63f, 13f)
                            horizontalLineToRelative(3.75f)
                            lineTo(18.5f, 8.65f)
                            lineTo(16.63f, 13f)
                            close()
                            moveToRelative(-13f, 0f)
                            horizontalLineTo(7.38f)
                            lineTo(5.5f, 8.65f)
                            lineTo(3.63f, 13f)
                            close()
                            moveTo(12f, 6f)
                            quadToRelative(0.43f, 0f, 0.71f, -0.29f)
                            quadTo(13f, 5.43f, 13f, 5f)
                            reflectiveQuadTo(12.71f, 4.29f)
                            reflectiveQuadTo(12f, 4f)
                            reflectiveQuadTo(11.29f, 4.29f)
                            reflectiveQuadTo(11f, 5f)
                            reflectiveQuadToRelative(0.29f, 0.71f)
                            reflectiveQuadTo(12f, 6f)
                            close()
                        }
                    }
                    .build()
            return _revenue_split!!
        }

    private var _revenue_split: ImageVector? = null

    private var _calendar_month: ImageVector? = null
}