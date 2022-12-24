package com.yama.marshal.tool

import com.yama.marshal.data.model.CartFullDetail
import com.yama.marshal.data.model.CourseFullDetail
import com.yama.marshal.screen.main.SortType.SortFleet
import com.yama.marshal.screen.main.SortType.SortHole
import com.yama.marshal.screen.main.SortType.SortHole.HOLE
import com.yama.marshal.screen.main.SortType.SortHole.PACE_OF_PLAY

class FleetSorter(private val sortFleet: SortFleet) : Comparator<CartFullDetail> {
    override fun compare(a: CartFullDetail, b: CartFullDetail): Int {
        return if (a.isFlag && !b.isFlag)
            -1
        else if (!a.isFlag && b.isFlag)
            1
        else
            when (sortFleet) {
                SortFleet.CAR -> byCardName(a, b)
                SortFleet.START_TIME -> byStartTime(a, b)
                SortFleet.PLACE_OF_PLAY -> byPaceOfPlay(a, b)
                SortFleet.HOLE -> byHole(a, b)
            }
    }

    private fun byCardName(a: CartFullDetail, b: CartFullDetail): Int {
        return byString(a.cartName, b.cartName)
    }
    
    private fun byStartTime(a: CartFullDetail, b: CartFullDetail): Int {
        if (a.state == CartFullDetail.State.inUse && b.state == CartFullDetail.State.inUse) {
            if (a.startTime == b.startTime)
                return 0
            return if (a.startTime!! < b.startTime!!) -1 else 1
        } else if (a.state == CartFullDetail.State.inUse && b.state != CartFullDetail.State.inUse) {
            return -1
        } else if (a.state != CartFullDetail.State.inUse && b.state == CartFullDetail.State.inUse) {
            return 1
        } else
            return byCardName(a, b)
    }

    private fun byPaceOfPlay(a: CartFullDetail, b: CartFullDetail): Int {
        var aTotalNetPace = a.totalNetPace
        var bTotalNetPace = b.totalNetPace
        var aState = a.state
        var bState = b.state

        if (a.returnAreaSts == 1 && !a.isOnClubHouse) {
            aState = CartFullDetail.State.notInUse
            aTotalNetPace = null
        }
        if (b.returnAreaSts == 1 && !b.isOnClubHouse) {
            bState = CartFullDetail.State.notInUse
            bTotalNetPace = null
        }

        if (aState == CartFullDetail.State.inUse && bState == CartFullDetail.State.inUse) {
            if (aTotalNetPace != null && bTotalNetPace != null) {

                if ((a.isFlag && b.isFlag) || (!a.isFlag && !b.isFlag)) {
                    if (aTotalNetPace == bTotalNetPace)
                        return 0
                    return if (aTotalNetPace > bTotalNetPace) -1 else 1
                } else if (a.isFlag) {
                    return -1
                } else return 1
            } else {
                return if ((a.isFlag && b.isFlag) || (!a.isFlag && !b.isFlag))
                    if (aTotalNetPace != null) -1 else 1
                else if (a.isFlag)
                    -1
                else
                    1
            }
        } else if (aState == CartFullDetail.State.inUse && bState != CartFullDetail.State.inUse) {
            return if (a.isFlag && b.isFlag)
                -1
            else if (a.isFlag)
                -1
            else if (b.isFlag)
                1
            else
                -1
        } else if (aState != CartFullDetail.State.inUse && bState == CartFullDetail.State.inUse) {
            return if (a.isFlag && b.isFlag)
                1
            else if (a.isFlag)
                -1
            else if (b.isFlag)
                1
            else
                1
        } else
            return byCardName(a, b)
    }

    private fun byHole(a: CartFullDetail, b: CartFullDetail): Int {
        return if (a.currPosHole != null && b.currPosHole == null)
            -1
        else if (a.currPosHole == null && b.currPosHole != null)
            1
        else if (a.currPosHole == b.currPosHole)
            0
        else if (a.currPosHole == null && b.currPosHole == null)
            0
        else if (a.currPosHole!! < b.currPosHole!! )
            -1
        else {
            1
        }
    }

    private fun byString(a: String, b: String): Int {
        val int1 = a.toIntOrNull()
        val int2 = b.toIntOrNull()

        if (int1 != null && int2 == null)
            return -1

        if (int1 == null && int2 != null) {
            return 1
        }

        if (int1 != null && int2 != null) {
            if (int1 == int2)
                return 0
            return if (int1 < int2) -1 else 1
        }

        return (a.compareTo(b, ignoreCase = true))
    }
}

class HoleSorter(private val sortHole: SortHole) : Comparator<CourseFullDetail.HoleData> {
    override fun compare(a: CourseFullDetail.HoleData, b: CourseFullDetail.HoleData): Int = when (sortHole) {
        HOLE -> byID(a, b)
        PACE_OF_PLAY -> holeByPaceOfPlay(a, b)
    }

    private fun byID(a: CourseFullDetail.HoleData, b: CourseFullDetail.HoleData): Int {
        return if (a.holeNumber < b.holeNumber)
            -1
        else if (a.holeNumber > b.holeNumber)
            1
        else
            0
    }

    private fun holeByPaceOfPlay(a: CourseFullDetail.HoleData, b: CourseFullDetail.HoleData): Int {
        if (a.differentialPace == b.differentialPace)
            return 0
        return if (a.differentialPace > b.differentialPace) -1 else 1

    }
}