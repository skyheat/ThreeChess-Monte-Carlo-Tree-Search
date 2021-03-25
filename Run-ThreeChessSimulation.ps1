[CmdletBinding()]
param (
    [parameter(Mandatory = $true)][int]$numberGames
)

for ($i = 0; $i -lt $numberGames; $i++) {
    invoke-expression 'cmd /c start powershell -Command { java -cp bin/ threeChess.ThreeChess }' 
}