#How it works:
#1. Generate all possible positions a number of moves from now
#2. Analyse positions to determine which ones are lost
#i. use pruning to discard moves that have a high loss rate
#3. Choose the move that results in the least lost positions

#Game information: Red and Blue checkers 6 rows and 7 columns 0 = red checker 1 = blue checker

#Software Development Plan
#1. Improve efficiency
    #Use pruning in decideNextMove
    #see if efficiency of genPositions and genNextMoves can be improved
#2. Create a user interface

def printPosition(position):
    for i in range(6):
        rowString = ""
        for j in range(7):
            if position[i][j] != '': rowString = rowString + '[' + str(position[i][j]) + ']'
            else: rowString = rowString + "[ ]"
        print(rowString)
    print()

def decideNextMove(position, colour, depth = 4):
    #Improvement ideas: alpha-beta pruning, only use even numbered depths, you can't lose on your move
    #1. call genNextMoves on position
    #2. for each move generated call genPositions
    #3. analyse the loss rate of each move using analysePosition
    #4. choose the move with the least loss rate
    moves = genNextMoves(position, colour)
    moveLossRate = []*len(moves)
    if colour == 1: lossColour = 0
    else: lossColour = 1
    for i in range(len(moves)):
        positions = genPositions(moves[i], colour, depth)
        losses = 0
        other = 0
        for k in range(len(positions)):
            result = analysePosition(positions[k])
            if result == lossColour: losses = losses + 1
            else: other = other + 1
        if other == 0: moveLossRate.append(1)
        else: moveLossRate.append(losses/(losses + other))
    return moveLossRate

def genPositions(position, colour, depth = 6):
    #Repeatedly call genNextMoves until we reach the depth, then return the positions to decideNextMove which analyses the positions using analysePosition
    #Note we don't have to account for games won before they reach the full depth, genNextMoves will simply
    #generate 'dummy' positions that decideNextMove will recognise as a win
    #Optimise
    positions = [position]
    for i in range(depth):
        if i > 0:
            positions.append(genNextMoves(positions[i][0], colour))
            for k in range(1, 7**i):
                positions[i+1].extend(genNextMoves(positions[i][k], colour)) #extend probably isn't being used correctly
        else: positions.append(genNextMoves(positions[i], colour))
        if colour == 1: colour = 0
        else: colour = 1
    return positions[depth]

def genNextMoves(position, colour):
    #returns an array of the positions possible in the next move
    #Optimise
    positions = [0,0,0,0,0,0,0]
    for i in range(7):
        if position[0][i]:
            positions[i] = False
            continue #check if top row is full
        positions[i] = [position[0][:], position[1][:], position[2][:], position[3][:], position[4][:], position[5][:]] #Needed because array assignment in python is bs
        for k in range(5,0,-1): #put checker in lowest row possible
            if positions[i][k][i] == 0 or positions[i][k][i] == 1: continue
            positions[i][k][i] = colour
            break
    return positions

def analysePosition(position):
    #take in a multidimensional array of a position and determine whether it is won for either side 0 = won for Red 1 = won for Blue 2 = even
    #1. check rows
    fullRows = 6
    for j in range(0,6):
        if position[j].count('') == 7: fullRows -= 1
        elif position[j].count(0) > 3:
            if connected(position[j], 0): return 0
        elif position[j].count(1) > 3:
            if connected(position[j], 1): return 1
    #2. check columns
    for k in range(0,7):
        column = []
        for l in range(5, 5 - fullRows, -1): column.append(position[l][k])
        if column.count(0) > 3:
            if connected(column, 0): return 0
        elif column.count(1) > 3:
            if connected(column, 1): return 1
    #3. check diagonals, I'm repeating a codeblock here instead of using a function because it is more efficient
    if fullRows < 4: return 2
    #check top left (going down) diagonals
    for m in range(0,3):
        diagonal = []
        for n in range(6-fullRows, 6):
            if (n-m) < 0: continue
            diagonal.append(position[n][n - m])
        if diagonal.count(0) > 3:
            if connected(diagonal, 0): return 0
        elif diagonal.count(1) > 3:
            if connected(diagonal, 1): return 1
    for q in range(1,4):
        if (7-q) - (6 - fullRows) < 4: break #if diagonals get shorter than 4 checkers we don't have to check them
        diagonal = []
        for r in range(6 - fullRows, 7 - q): diagonal.append(position[r][r+q])
        if diagonal.count(0) > 3:
            if connected(diagonal, 0): return 0
        elif diagonal.count(1) > 3:
            if connected(diagonal, 1): return 1
    #check top right (going down) diagonals
    for o in range(0,3):
        diagonal = []
        for p in range(6-fullRows, 6):
            if (p-o) < 0: continue
            diagonal.append(position[p][6 - (p-o)])
        if diagonal.count(0) > 3:
            if connected(diagonal, 0): return 0
        elif diagonal.count(1) > 3:
            if connected(diagonal, 1): return 1                                 
    for s in range(1,4):
        if (7-s) - (6 - fullRows) < 4: break
        diagonal = []
        for t in range(6 - fullRows, 7 - s): diagonal.append(position[t][6 - (s + t)])
        if diagonal.count(0) > 3:
            if connected(diagonal, 0): return 0
        elif diagonal.count(1) > 3:
            if connected(diagonal, 1): return 1                         
    return 2

def connected(line, colour):
    #checks if 4 of the same colour checkers are connected
    #fully optimised, might look bad but this function is run millions of times so it needs to be as fast as possible
    i = 0
    length = len(line) - 3
    while i < length:
        if line[i] == colour:
            if line[i+1] == colour:
                if line[i+2] == colour:
                    if line[i+3] == colour: return True
                    return False
                i += 1
            i += 1
        i += 1
    return False


samplePosition = [['','','','','','',''],
                  ['','','','','','',''],
                  [1 ,1 ,'','','',1 ,1 ],
                  [0 ,0 ,1 ,'','',1 ,1 ],
                  [0 ,1 ,1 ,1 ,0 ,0 ,0 ],
                  [0 ,0 ,0 ,1 ,0 ,1 ,1 ]]

startPosition = [['','','','','','',''],
                ['','','','','','',''],
                ['','','','','','',''],
                ['','','','','','',''],
                ['','','','','','',''],
                ['','','','','','','']]
