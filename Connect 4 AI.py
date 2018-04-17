from colorama import init, Fore, Style, Back
init()

def updateColour(colour):
    if colour == 1: return 0
    return 1

def printPosition(position):
    print(Back.WHITE, end = '')
    for i in range(6):
        for j in range(7):
            if position[i][j] == '': print(Fore.BLACK + "[ ]", end = '')
            else:
                print(Fore.BLACK + "[", end = '')
                if position[i][j] == 1:
                    print(Fore.RED +'O', end = '')
                else:
                    print(Fore.BLUE + 'O', end = '')
                print(Fore.BLACK + "]", end = '')
        print()
    print(Fore.BLACK + " 0  1  2  3  4  5  6 ")
    print(Style.RESET_ALL, end = '')

def getUserMove(position, colour):
    #Full input error checking
    printPosition(position)
    while True:
        move = input("Enter your move: ")
        try:
            move = int(move)
            if move > 6:
                print("Please enter a valid column number between 0-6")
                continue
            if position[0][move] != '':
                print("Column is full, Please enter a valid column number")
                continue
            break
        except:
            print("Please enter a valid column number between 0-6")
    return updatePosition(position, move, colour)

def updatePosition(position, move, colour):
    #Takes in a position and adds a checker in the 'move' column
    emptySpaces = [position[1][move], position[2][move], position[3][move], position[4][move], position[5][move]].count('')
    position[emptySpaces][move] = colour
    return position

def decideNextMove(position, colour, depth = 5):
    #1. call genNextMoves on position
    #2. for each move generated call genPositions
    #3. analyse the loss rate of each move using analysePosition
    #4. choose the move with the least loss rate
    
    moves = genNextMoves(position, colour)
    moveLossRate = []*len(moves)
    lossColour = updateColour(colour)
    
    for i in range(len(moves)):
        #analyse the lossRate of a move
        positions = genPositions(moves[i], colour, depth)
        losses = 0
        other = 0
        for k in range(len(positions)):
            result = analysePosition(positions[k])
            if result == lossColour: losses = losses + 1
            else: other = other + 1
        moveLossRate.append(losses/(losses + other))
    
    move = moveLossRate.index(min(moveLossRate))
    #must column correct the move to take into account starting positions with full rows who did not analyse 7 moves
    i = 0
    while i <= move:
        if position[0][i] != '': move += 1
        i += 1
    return move

def genPositions(position, colour, depth = 5):
    #Repeatedly call genNextMoves until we reach the depth
    positions = [[position]]
    
    #Loop through and add positions as the next element in array (array looks like this [positions of depth0, positions of depth1, positions of depth2, ...])
    for i in range(depth):
        positions.append(genNextMoves(positions[i][0], colour))
        for k in range(1, len(positions[i])):
            positions[i+1].extend(genNextMoves(positions[i][k], colour))
        colour = updateColour(colour)
    return positions[depth]

def genNextMoves(position, colour):
    #returns an array of the positions possible in the next move
    #completedColumns avoids creating duplicate positions i.e. adding a checker in c1 then c2 is the same as c2 then c1
    positions = []
    for i in range(7):
        if position[0][i] != '': continue #check if top row is full
        
        positions.append([position[0][:], position[1][:], position[2][:], position[3][:], position[4][:], position[5][:]]) #Append a copy of position that is not linked to position
        positions[-1] = updatePosition(positions[-1], i, colour) #add a checker in column i
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
    if fullRows < 4: return 2 #can't have 4 in a row if less than 4 rows
    
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


samplePosition = [[1 ,'','','','','',''],
                  [0 ,'','','','','',''],
                  [1 ,1 ,'','','',1 ,1 ],
                  [0 ,0 ,1 ,'','',1 ,1 ],
                  [0 ,1 ,1 ,1 ,0 ,0 ,0 ],
                  [0 ,0 ,0 ,1 ,0 ,1 ,1 ]]

samplePosition2 = [[1,'','',0 ,'','',''],
                   [1,'','',1 ,'','',''],
                   [0,'','',1 ,'','',''],
                   [1,'','',0 ,'',1 ,''],
                   [1,'','',0 ,'',1 ,''],
                   [1,'','',0 ,'',1 ,'']]

startPosition = [['','','','','','',''],
                ['','','','','','',''],
                ['','','','','','',''],
                ['','','','','','',''],
                ['','','','','','',''],
                ['','','','','','','']]

def mainLine(position):
    print("Welcome to connect 4! Input your moves by entering the column number you wish to place a checker in, columns are numbered 0 to 6 left to right")
    colour = 1
    while position[0].count('') != 0: #while game is not over
        position = getUserMove(position, colour)
        printPosition(position)
        #check if game won
        if analysePosition(position) == colour:
            print("--------You win!--------")
            return
        
        colour = updateColour(colour)
        move = decideNextMove(position, colour)
        position = updatePosition(position, move, colour)
        #check if game won
        if analysePosition(position) == colour:
            print("--------You lose :(--------")
            return
        
        colour = updateColour(colour)

#mainLine(startPosition)
n = input("Press enter to quit")
