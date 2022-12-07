package com.yama.marshal.tool

import com.yama.marshal.data.model.CartFullDetail
import com.yama.marshal.screen.fleet_list.SortFleet

class FleetSorter(private val sortFleet: SortFleet) : Comparator<CartFullDetail> {
    override fun compare(a: CartFullDetail, b: CartFullDetail): Int = when (sortFleet) {
        SortFleet.CAR -> byCardName(a, b)
        SortFleet.START_TIME -> byStartTime(a, b)
        SortFleet.PLACE_OF_PLAY -> byPaceOfPlay(a, b)
        SortFleet.HOLE -> byHole(a, b)
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

                if ((a.flagCart && b.flagCart) || (!a.flagCart && !b.flagCart)) {
                    if (aTotalNetPace == bTotalNetPace)
                        return 0
                    return if (aTotalNetPace > bTotalNetPace) -1 else 1
                } else if (a.flagCart) {
                    return -1
                } else return 1
            } else {
                return if ((a.flagCart && b.flagCart) || (!a.flagCart && !b.flagCart))
                    if (aTotalNetPace != null) -1 else 1
                else if (a.flagCart)
                    -1
                else
                    1
            }
        } else if (aState == CartFullDetail.State.inUse && bState != CartFullDetail.State.inUse) {
            return if (a.flagCart && b.flagCart)
                -1
            else if (a.flagCart)
                -1
            else if (b.flagCart)
                1
            else
                -1
        } else if (aState != CartFullDetail.State.inUse && bState == CartFullDetail.State.inUse) {
            return if (a.flagCart && b.flagCart)
                1
            else if (a.flagCart)
                -1
            else if (b.flagCart)
                1
            else
                1
        } else
            return byCardName(a, b)
    }

    private fun byHole(a: CartFullDetail, b: CartFullDetail): Int {
        return if (a.currPosHole == null)
            1
        else if (b.currPosHole == null)
            -1
        else if (a.currPosHole == b.currPosHole) {
            0
        } else if (a.currPosHole < b.currPosHole) {
            -1
        } else {
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