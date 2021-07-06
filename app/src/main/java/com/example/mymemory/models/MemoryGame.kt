package com.example.mymemory.models

import com.example.mymemory.utils.DEFAULT_ICONS

class MemoryGame(private val boardSize: BoardSize) {
    val cards: List<MemoryCard>
    var numPairsFound = 0

    private var numCardFlips = 0
    private var indexOfSingleFlippedCard: Int? = null

    init {

        val chosenImages = DEFAULT_ICONS.shuffled().take(boardSize.getNumPairs())
        val randomisedImages = (chosenImages + chosenImages).shuffled()
        cards = randomisedImages.map { MemoryCard(it) }
    }

    fun flipCard(position: Int): Boolean {
        ++numCardFlips
        val card = cards[position]
        var foundMatch: Boolean = false
        if( indexOfSingleFlippedCard == null ) {
            restoreCards()
            indexOfSingleFlippedCard = position
        } else {
            foundMatch = checkForMatch(indexOfSingleFlippedCard!!, position)
            indexOfSingleFlippedCard = null
        }
        card.isFaceUp = !card.isFaceUp
        return foundMatch
    }

    private fun checkForMatch(pos1: Int, pos2: Int): Boolean {
        if( cards[pos1].identifier != cards[pos2].identifier ) return false

        cards[pos1].isMatched = true
        cards[pos2].isMatched = true
        numPairsFound++
        return true
    }

    private fun restoreCards() {
        for( card in cards ) {
            if( !card.isMatched ) {
                card.isFaceUp = false
            }
        }
    }

    fun haveWonGame(): Boolean {
        return numPairsFound == boardSize.getNumPairs()
    }

    fun isCardFaceUp(position: Int): Boolean {
        return cards[position].isFaceUp
    }

    fun getNumMoves(): Int {
        return numCardFlips/2
    }
}
