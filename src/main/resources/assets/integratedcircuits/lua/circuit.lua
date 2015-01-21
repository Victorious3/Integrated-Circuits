local version = "0.1"
local twidth, theight = term.getSize();
local targs = {...}
local validSides = {"left", "right", "back", "front", "top", "bottom"}
local circuit

-- Get circuit peripheral
if #targs > 0 then
  if validSides[targs[1]] ~= nil then 
    local type = peripheral.getType(targs[1])
    if type == "IC Circuit" then circuit = peripheral.wrap(targs[1]) end
  end
else
  for k, s in pairs(validSides) do
    local type = peripheral.getType(s)
    if type == "IC Circuit" then circuit = peripheral.wrap(s) end
    if circuit ~= nil then break end
  end
end

if circuit == nil then
  error("No connected circuit peripheral found!")
end

local csize = circuit:getSize()
local name = circuit:getName()
local author = circuit:getAuthor()

local autoRedraw = 0
local redrawTimer = nil
local curX = 1
local curY = -5

-- Function declaration
-- Util functions
function drawStringAt(x, y, string, tcolor, bcolor)
  term.setCursorPos(x, y)
  if tcolor ~= nil then term.setTextColor(tcolor) end
  if bcolor ~= nil then term.setBackgroundColor(bcolor) end
  term.write(string)
end

--Check if a gate receives power from any side
function isPowered(x, y)
  a, b, c, d = circuit:getPowerTo(x, y)
  return a or b or c or d
end

function startswith(string, match) 
    return string:find('^' .. match) ~= nil
end

-- Main functions
function drawCircuit(ox, oy)
  for x = 0, csize - 1 do
    for y = 0, csize - 1 do
      term.setCursorPos(x + ox, y + oy)
      gname, gid, gmeta = circuit:getGateAt(x, y)
      
      if startswith(gname, "wire") then
        local gcolor = tonumber(gname:sub(6))
        local gpower = isPowered(x, y)
        local color = colors.black

        if gcolor == 0 then 
          if gpower then color = colors.lime else color = colors.green end 
        elseif gcolor == 1 then
          if gpower then color = colors.pink else color = colors.red end
        elseif gcolor == 2 then
          if gpower then color = colors.yellow else color = colors.orange end
        end
        
        term.setBackgroundColor(color)
        term.write(" ")
      elseif gname ~= "null" then
        term.write("X")
      end
      term.setBackgroundColor(colors.black)
    end
  end
end

function drawScreen()
  term.clear()
  
  drawCircuit(curX, curY)
  
  paintutils.drawLine(1, 1, twidth, 1, colors.blue) 
  local title = "ICP V"..version
  drawStringAt(2, 1, title, colors.black)
  drawStringAt(3 + title:len(), 1, name.." ("..csize.."x"..csize..") by "..author, colors.white)
  drawStringAt(twidth, 1, "X", colors.white, colors.red)
end

function onMousePressed(x, y)
  
end

function eventListener()
  while true do
    local event, par1, par2, par3 = os.pullEvent()
    
    if event == "timer" and par2 == redrawTimer then
      drawScreen()
      if autoRedraw > 0 then
        redrawTimer = os.startTimer(autoRedraw)
      end
    end
    
    if event == "term_resize" then
      twidth, theight = term.getSize();
      drawScreen()
    end
    
    if event == "mouse_click" then
      if par2 == twidth and par3 == 1 then
        term.setCursorPos(1, 1)
        term.setTextColor(colors.white)
        term.setBackgroundColor(colors.black)
        term.clear()
        return
      else onMousePressed(par2, par3) end
    end
  end
end

drawScreen()
eventListener()