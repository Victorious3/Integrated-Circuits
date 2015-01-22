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

-- Function declaration
-- Util functions
function drawStringAt(x, y, string, tcolor, bcolor)
  term.setCursorPos(x, y)
  if tcolor ~= nil then term.setTextColor(tcolor) end
  if bcolor ~= nil then term.setBackgroundColor(bcolor) end
  term.write(string)
end

-- Check if a gate receives power from any side
function isPowered(x, y)
  local a, b, c, d = circuit.getPowerTo(x, y)
  return a or b or c or d
end

function startswith(string, match) 
  return string:find('^' .. match) ~= nil
end

function getGateChar(id)
  local ascii = id + 65 + 3
  if ascii > 90 then ascii = ascii + 6 end
  return string.char(ascii)
end

local csize = circuit.getSize()
local name = circuit.getName()
local author = circuit.getAuthor()

local fbuffer = window.create(term.current(), 1, 1, csize, csize, false)
local autoRedraw = 0
local curX = 1
local curY = -5

local isgWindowOpen = false
local blinkTimer

-- Main functions

function drawGateAt(x, y, gname, gid, gmeta, ox, oy)
  ox, oy = ox or x + 1, oy or y + 1
  term.setCursorPos(ox, oy)
  term.setBackgroundColor(colors.gray)
  if startswith(gname, "wire") then
      local gcolor = tonumber(gname:sub(6))
      local gpower = isPowered(x, y)
      local color = 0

      if gcolor == 0 then 
        color = gpower and colors.lime or colors.green
      elseif gcolor == 1 then
        color = gpower and colors.pink or colors.red
      elseif gcolor == 2 then
        color = gpower and colors.yellow or colors.orange
      end
      
      term.setBackgroundColor(color)
      term.write(" ")
    elseif gname == "iobit" then
      local frequency = circuit.getGateProperty(x, y, "FREQUENCY")
      local side = (circuit.getGateProperty(x, y, "ROTATION") + 2) % 4
      
      local input = {circuit.getInputFromSide(side)}
      local output = {circuit.getOutputToSide(side)}
      
      local color = 0
      local gpower = input[frequency + 1] ~= 0 or output[frequency + 1] ~= 0
      if gpower then color = colors.lime else color = colors.green end 
      
      term.setBackgroundColor(color)
      term.write(string.format("%X", frequency))
    elseif gname ~= "null" then
      term.setBackgroundColor(colors.black)
      term.write(getGateChar(gid))
    else
      term.write(" ")
    end
end

function updateCircuit()
  local _term = term.redirect(fbuffer)
  for x = 0, csize - 1 do
    for y = 0, csize - 1 do
      local gname, gid, gmeta = circuit.getGateAt(x, y)
      drawGateAt(x, y, gname, gid, gmeta)
    end
  end
  term.redirect(_term)
end

local gx, gy, gname, gid, gmeta
local gproperties
local gwindowpos = 15
local gwcolumn
local gwstring

function drawGateWindow()
  term.setBackgroundColor(colors.lightGray)
  
  for i = 1, theight do
    paintutils.drawLine(twidth - gwindowpos, i, twidth - gwindowpos + gwindowpos, i, colors.lightGray)
  end
  
  drawStringAt(twidth - gwindowpos + 3, 3, gname)
  drawGateAt(gx, gy, gname, gid, gmeta, twidth - gwindowpos + 1, 3);
  term.setBackgroundColor(colors.lightGray)
  for i = 1, #gproperties do
    local pname = gproperties[i]
    local pvalue, ptype = circuit.getGateProperty(gx, gy, pname)
    ptype = ptype:sub(1, 1)
    pvalue = tostring(pvalue)
    drawStringAt(twidth - #pvalue, i + 4, pvalue, colors.black)
    if #pvalue + #pname > gwindowpos - 4 then
      pname = pname:sub(0, gwindowpos - 7 - #pvalue).."..."
    end
    drawStringAt(twidth - gwindowpos + 1, i + 4, ptype.." "..pname, colors.black)
  end
  if gwcolumn then
    local pname = gproperties[gwcolumn]
    local pvalue, ptype = circuit.getGateProperty(gx, gy, pname)
    pname = ptype:sub(1, 1).." "..pname
    
    if #pname > gwindowpos - 1 then
      pname = pname:sub(0, gwindowpos - 4).."..."
    end
   
    paintutils.drawLine(twidth - gwindowpos + 1, gwcolumn + 4, twidth - 1, gwcolumn + 4, colors.gray)
    drawStringAt(twidth - gwindowpos + 1, gwcolumn + 4, pname, colors.white)
    paintutils.drawLine(twidth - gwindowpos + 1, gwcolumn + 5, twidth - 1, gwcolumn + 5, colors.black)
    drawStringAt(twidth - gwindowpos + 1, gwcolumn + 5, gwstring.."_", colors.white)
  end
end

function drawCircuit(ox, oy)
  fbuffer.reposition(ox, oy)
  fbuffer.setVisible(true)
  fbuffer.redraw()
  fbuffer.setVisible(false)
end

function drawScreen()
  term.setBackgroundColor(colors.black)
  term.clear()
  drawCircuit(curX, curY)
  if isgWindowOpen then drawGateWindow() end
  
  paintutils.drawLine(1, 1, twidth, 1, colors.blue) 
  local title = "ICX V"..version
  drawStringAt(2, 1, title, colors.black)
  drawStringAt(3 + #title, 1, name.." ("..csize.."x"..csize..") by "..author, colors.white)
  drawStringAt(twidth, 1, "X", colors.white, colors.red)
end

function onMouseClickedGW(button, mx, my)
  if button == 1 then
    local column = my - 4;
    if gproperties[column] then
      gwcolumn = column
      gwstring = tostring(circuit.getGateProperty(gx, gy, gproperties[column]))
      drawScreen()
    end
  end
end

function onKeyPressed(char, key)
  if key == 63 then --F5
    updateCircuit()
    drawScreen()
  elseif key == 14 and isgWindowOpen and gwcolumn then --BACKSPACE
    local len = #gwstring - 1;
    if len >= 0 then
      gwstring = gwstring:sub(0, len)
    end
    drawScreen()
  elseif key == 28 and isgWindowOpen then --ENTER
    if gwcolumn then
      local pname = gproperties[gwcolumn]
      local pvalue, ptype = circuit.getGateProperty(gx, gy, pname)
      if ptype == "BooleanProperty" then
        local val
        if gwstring == "true" or gwstring == "1" then val = true
        elseif gwstring == "false" or gwstring == "0" then val = false end
        pcall(circuit.setGateProperty, gx, gy, pname, val)
      else
        local err, val = pcall(tonumber, gwstring)
        pcall(circuit.setGateProperty, gx, gy, pname, val)
      end
      gname, gid, gmeta = circuit.getGateAt(gx, gy)
      updateCircuit()
      gwcolumn = nil
    else
      isgWindowOpen = false
    end
    drawScreen()
  elseif char then
    if isgWindowOpen and gwcolumn and #gwstring < gwindowpos - 2 then
      gwstring = gwstring..char
      drawScreen()
    end
  end
end

function onMouseClicked(button, mx, my)
  if isgWindowOpen then
      if mx >= twidth - gwindowpos then
        onMouseClickedGW(button, mx, my)
        return
      elseif button == 1 then return end
      drawGateAt(gx, gy, gname, gid, gmeta, curX + gx, curY + gy)
  end
  
  local tx, ty = mx - curX, my - curY
  if tx >= 0 and ty >= 0 and tx < csize and ty < csize then
    gname, gid, gmeta = circuit.getGateAt(tx, ty)
    
    if gname == "null" then 
      if isgWindowOpen then isgWindowOpen = false end
      drawScreen()
      return 
    end
    
    gx, gy = tx, ty
    
    if button == 1 and not isgWindowOpen then
      drawScreen()
      term.setCursorPos(mx, my)
      term.setBackgroundColor(colors.blue)
      term.write(gname)
    elseif button == 2 then
      isgWindowOpen = true
      blinkTimer = os.startTimer(0.5)
      gproperties = {circuit.getGateProperties(gx, gy)}
      drawScreen()
      term.setCursorPos(gx + curX, gy + curY)
      term.setBackgroundColor(colors.blue)
      term.write(" ")
    end
  else drawScreen() end
end

function eventListener()
  
  local lastX, lastY
  local redrawTimer = nil
  local blinkToggle = false
  
  while true do
    local event, par1, par2, par3 = os.pullEvent()
    
    if event == "timer" then
      if par1 == redrawTimer then
        updateCircuit()
        drawScreen()
        if autoRedraw > 0 then
          redrawTimer = os.startTimer(autoRedraw)
        end
      elseif par1 == blinkTimer and isgWindowOpen then
        if gx + curX < twidth - gwindowpos then
          if(blinkToggle) then
            drawStringAt(gx + curX, gy + curY, " ", colors.white, colors.blue)
            if gwcolumn then
              drawStringAt(twidth - gwindowpos + 1 + #gwstring, gwcolumn + 5, "_", colors.white, colors.black)
            end
          else 
            drawGateAt(gx, gy, gname, gid, gmeta, curX + gx, curY + gy)    
            if gwcolumn then
              drawStringAt(twidth - gwindowpos + 1 + #gwstring, gwcolumn + 5, " ", colors.white, colors.black)
            end
          end
          
          blinkToggle = not blinkToggle
        end
        blinkTimer = os.startTimer(0.5)
      end
    elseif event == "term_resize" then
      twidth, theight = term.getSize();
      drawScreen()
    elseif event == "mouse_click" then
      lastX, lastY = par2, par3
      if par2 == twidth and par3 == 1 then
        term.setCursorPos(1, 1)
        term.setTextColor(colors.white)
        term.setBackgroundColor(colors.black)
        term.clear()
        return
      else onMouseClicked(par1, par2, par3) end
    elseif event == "mouse_drag" and par1 ~= 2 then
      local offX, offY = par2 - lastX, par3 - lastY
      curX = curX + offX
      curY = curY + offY
      lastX, lastY = par2, par3
      drawScreen()
    elseif event == "key" then
      onKeyPressed(nil, par1)
    elseif event == "char" then
      onKeyPressed(par1)
    end
  end
end

updateCircuit()
drawScreen()
eventListener()